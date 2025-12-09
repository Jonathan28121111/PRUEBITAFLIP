package flip7.game;

import flip7.common.*;
import java.util.*;

public class GameLogic {
    private GameState gameState = new GameState();
    private Deck deck = new Deck();
    private List<GameEventListener> listeners = new ArrayList<>();
    private boolean waitingForActionTarget = false;
    private int playerWaitingForAction = -1;
    private Card pendingActionCard = null;
    
    // Control de FLIP 3
    private boolean isProcessingFlip3 = false;
    private int flip3PlayerID = -1;
    private int flip3CardsRemaining = 0;
    
    public interface GameEventListener {
        void onCardDealt(int playerId, Card card);
        void onPlayerBusted(int playerId, Card card);
        void onPlayerStand(int playerId);
        void onPlayerFrozen(int playerId);
        void onActionCardDrawn(int playerId, Card card);
        void onRoundEnd(List<Player> players, int roundNumber);
        void onGameEnd(Player winner);
        void onTurnChange(int playerId);
        void onGameStateUpdate(GameState state);
        void onNeedActionTarget(int playerId, Card card, List<Player> activePlayers);
    }
    
    public void addListener(GameEventListener l) { listeners.add(l); }
    
    public int addPlayer(String name) {
        int id = gameState.getPlayers().size();
        gameState.getPlayers().add(new Player(name, id));
        return id;
    }
    
    public void removePlayer(int id) {
        Player p = gameState.getPlayerById(id);
        if (p != null) p.setConnected(false);
    }
    
    public boolean canStartGame() { return gameState.getPlayers().size() >= 2; }
    
    public void startGame() {
        if (!canStartGame()) return;
        gameState.setPhase(GameState.Phase.DEALING);
        gameState.setRoundNumber(1);
        deck.reset();
        startRound();
    }
    
    public void startRound() {
        for (Player p : gameState.getPlayers()) p.resetForNewRound();
        gameState.setPhase(GameState.Phase.DEALING);
        
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        isProcessingFlip3 = false;
        flip3PlayerID = -1;
        flip3CardsRemaining = 0;
        
        dealInitialCards();
        
        gameState.setPhase(GameState.Phase.PLAYING);
        
        int startIdx = gameState.getDealerIndex();
        Player startPlayer = gameState.getPlayers().get(startIdx);
        if (!startPlayer.isConnected()) {
            startIdx = getNextActivePlayerIndex(startIdx);
            if (startIdx == -1) startIdx = 0;
        }
        
        gameState.setCurrentPlayerIndex(startIdx);
        notifyTurnChange();
        notifyGameStateUpdate();
    }
    
    private void dealInitialCards() {
        List<Player> players = gameState.getPlayers();
        int start = (gameState.getDealerIndex() + 1) % players.size();
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get((start + i) % players.size());
            if (!player.isConnected()) continue;
            
            Card card = deck.drawCard();
            if (card != null) {
                // En reparto inicial, cartas de acción se descartan
                if (card.isActionCard()) {
                    deck.discard(card);
                    i--; // Volver a intentar
                } else {
                    boolean success = player.addCard(card);
                    if (success) {
                        notifyCardDealt(player.getId(), card);
                    }
                }
            }
        }
        updateDeckInfo();
    }
    
    public void playerHit(int playerId) {
        if (waitingForActionTarget) return;
        
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) return;
        if (!player.isActive()) return;
        
        Card card = deck.drawCard();
        if (card == null) return;
        
        processCardForPlayer(player, card, false);
        
        if (player.hasFlip7() || gameState.isRoundOver()) {
            endRound();
            return;
        }
        
        // Si no estamos esperando acción y no es FLIP 3, pasar turno
        if (!waitingForActionTarget && !isProcessingFlip3) {
            advanceTurn();
            notifyGameStateUpdate();
        }
    }
    
    private void processCardForPlayer(Player player, Card card, boolean isFromFlip3) {
        if (card.isActionCard()) {
            notifyCardDealt(player.getId(), card);
            notifyActionCardDrawn(player.getId(), card);
            
            // SECOND CHANCE: automática si no tienes una
            if (card.getType() == Card.CardType.SECOND_CHANCE) {
                if (!player.hasSecondChance()) {
                    // Dársela automáticamente
                    player.setSecondChanceCard(card);
                    notifyCardDealt(player.getId(), card);
                    updateDeckInfo();
                    notifyGameStateUpdate();
                    return;
                }
                // Ya tiene una, debe elegir a quién darla (NO puede ser él mismo)
            }
            
            // Obtener jugadores válidos para asignar
            List<Player> validTargets = getValidTargetsForAction(player, card);
            
            if (validTargets.isEmpty()) {
                // Nadie puede recibirla, descartar
                deck.discard(card);
                updateDeckInfo();
                return;
            }
            
            if (validTargets.size() == 1) {
                // Solo un objetivo posible, asignar automáticamente
                applyActionCard(validTargets.get(0).getId(), card, player.getId());
                
                // Si estamos en FLIP 3, continuar
                if (isProcessingFlip3 && flip3CardsRemaining > 0) {
                    continueFlipThree(player);
                }
            } else {
                // Múltiples objetivos, pedir selección
                waitingForActionTarget = true;
                playerWaitingForAction = player.getId();
                pendingActionCard = card;
                notifyNeedActionTarget(player.getId(), card, validTargets);
            }
        } else {
            // Carta numérica o modificadora
            boolean success = player.addCard(card);
            if (!success && card.isNumberCard()) {
                handleBust(player, card);
            } else {
                notifyCardDealt(player.getId(), card);
            }
        }
        updateDeckInfo();
    }
    
    private List<Player> getValidTargetsForAction(Player fromPlayer, Card card) {
        List<Player> valid = new ArrayList<>();
        
        for (Player p : gameState.getPlayers()) {
            if (!p.isConnected()) continue;
            if (!p.isActive() && card.getType() != Card.CardType.SECOND_CHANCE) continue;
            
            if (card.getType() == Card.CardType.SECOND_CHANCE) {
                // SECOND CHANCE: solo jugadores que NO tengan una
                // Si el que la sacó ya tiene una, él NO puede ser objetivo
                if (!p.hasSecondChance()) {
                    if (fromPlayer.hasSecondChance() && p.getId() == fromPlayer.getId()) {
                        // El jugador ya tiene una, no puede dársela a sí mismo
                        continue;
                    }
                    valid.add(p);
                }
            } else {
                // FREEZE y FLIP 3: cualquier jugador activo (incluyéndose)
                if (p.isActive()) {
                    valid.add(p);
                }
            }
        }
        
        return valid;
    }
    
    public void assignActionCard(int fromId, int targetId, Card card) {
        if (!waitingForActionTarget || playerWaitingForAction != fromId) return;
        
        Player target = gameState.getPlayerById(targetId);
        Player fromPlayer = gameState.getPlayerById(fromId);
        if (target == null || fromPlayer == null) return;
        
        // Validar que el objetivo es válido
        if (card.getType() == Card.CardType.SECOND_CHANCE) {
            if (target.hasSecondChance()) return; // Ya tiene una
            if (fromPlayer.hasSecondChance() && targetId == fromId) return; // No puede dársela a sí mismo si ya tiene
        }
        
        // Limpiar estado de espera
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        
        applyActionCard(targetId, card, fromId);
        
        // Si estábamos en FLIP 3, continuar con las cartas restantes
        if (isProcessingFlip3 && flip3CardsRemaining > 0) {
            Player flip3Player = gameState.getPlayerById(flip3PlayerID);
            if (flip3Player != null && !flip3Player.isBusted()) {
                continueFlipThree(flip3Player);
                return;
            }
        }
        
        // FLIP 3 terminó o no estábamos en uno, pasar turno
        // Pero verificar si congelamos al único otro jugador
        if (!isProcessingFlip3) {
            advanceTurnAfterAction(fromId);
        }
        
        notifyGameStateUpdate();
    }
    
    private void applyActionCard(int targetId, Card card, int fromPlayerId) {
        Player target = gameState.getPlayerById(targetId);
        if (target == null) return;
        
        switch (card.getType()) {
            case FREEZE:
                target.addActionCard(card);
                target.setFrozen(true);
                notifyCardDealt(targetId, card);
                notifyPlayerFrozen(targetId);
                break;
                
            case FLIP_THREE:
                target.addActionCard(card);
                notifyCardDealt(targetId, card);
                
                // Si me lo doy a mí mismo durante mi turno
                if (targetId == fromPlayerId) {
                    startFlipThree(target);
                } else {
                    // Se lo di a otro, él lo ejecutará en su turno? 
                    // No, FLIP 3 se ejecuta inmediatamente
                    startFlipThreeForOther(target, fromPlayerId);
                }
                break;
                
            case SECOND_CHANCE:
                target.setSecondChanceCard(card);
                notifyCardDealt(targetId, card);
                break;
        }
    }
    
    private void startFlipThree(Player player) {
        isProcessingFlip3 = true;
        flip3PlayerID = player.getId();
        flip3CardsRemaining = 3;
        continueFlipThree(player);
    }
    
    private void startFlipThreeForOther(Player targetPlayer, int originalPlayerId) {
        // FLIP 3 dado a otro jugador se ejecuta inmediatamente
        isProcessingFlip3 = true;
        flip3PlayerID = targetPlayer.getId();
        flip3CardsRemaining = 3;
        continueFlipThree(targetPlayer);
        
        // Después del FLIP 3, el turno pasa al siguiente del jugador original
        if (!isProcessingFlip3) {
            // FLIP 3 ya terminó
            advanceTurnAfterAction(originalPlayerId);
        }
    }
    
    private void continueFlipThree(Player player) {
        while (flip3CardsRemaining > 0 && !player.isBusted() && !player.hasFlip7()) {
            Card card = deck.drawCard();
            if (card == null) break;
            
            flip3CardsRemaining--;
            
            if (card.isActionCard()) {
                notifyCardDealt(player.getId(), card);
                notifyActionCardDrawn(player.getId(), card);
                
                // SECOND CHANCE automática si no tiene
                if (card.getType() == Card.CardType.SECOND_CHANCE && !player.hasSecondChance()) {
                    player.setSecondChanceCard(card);
                    notifyGameStateUpdate();
                    continue; // Siguiente carta del FLIP 3
                }
                
                // Obtener objetivos válidos
                List<Player> validTargets = getValidTargetsForAction(player, card);
                
                if (validTargets.isEmpty()) {
                    deck.discard(card);
                    continue;
                }
                
                if (validTargets.size() == 1) {
                    // Solo un objetivo, asignar automáticamente
                    applyActionCardDuringFlip3(validTargets.get(0).getId(), card, player);
                    notifyGameStateUpdate();
                } else {
                    // Múltiples objetivos, pausar y pedir selección
                    waitingForActionTarget = true;
                    playerWaitingForAction = player.getId();
                    pendingActionCard = card;
                    notifyNeedActionTarget(player.getId(), card, validTargets);
                    return; // Salir y esperar asignación
                }
            } else {
                // Carta numérica o modificadora
                boolean success = player.addCard(card);
                notifyCardDealt(player.getId(), card);
                
                if (!success && card.isNumberCard()) {
                    player.getNumberCards().add(card);
                    player.setBusted(true);
                    notifyPlayerBusted(player.getId(), card);
                    break;
                }
            }
            
            notifyGameStateUpdate();
        }
        
        // FLIP 3 terminado
        isProcessingFlip3 = false;
        int finishedPlayerId = flip3PlayerID;
        flip3PlayerID = -1;
        flip3CardsRemaining = 0;
        
        if (player.hasFlip7()) {
            endRound();
            return;
        }
        
        // Después de FLIP 3, turno pasa al siguiente
        advanceTurn();
        notifyGameStateUpdate();
        updateDeckInfo();
    }
    
    private void applyActionCardDuringFlip3(int targetId, Card card, Player flip3Player) {
        Player target = gameState.getPlayerById(targetId);
        if (target == null) return;
        
        switch (card.getType()) {
            case FREEZE:
                target.addActionCard(card);
                target.setFrozen(true);
                notifyCardDealt(targetId, card);
                notifyPlayerFrozen(targetId);
                break;
                
            case FLIP_THREE:
                // FLIP 3 dentro de FLIP 3 - se acumulan las cartas
                target.addActionCard(card);
                notifyCardDealt(targetId, card);
                if (targetId == flip3Player.getId()) {
                    // Me lo doy a mí mismo, agregar 3 cartas más
                    flip3CardsRemaining += 3;
                } else {
                    // Se lo doy a otro... ejecutar su FLIP 3 después?
                    // Por simplicidad, agregar las 3 cartas al actual
                    flip3CardsRemaining += 3;
                }
                break;
                
            case SECOND_CHANCE:
                target.setSecondChanceCard(card);
                notifyCardDealt(targetId, card);
                break;
        }
    }
    
    private void advanceTurnAfterAction(int actionPlayerId) {
        // Verificar si hay más de un jugador activo
        List<Player> activePlayers = gameState.getActivePlayers();
        
        if (activePlayers.size() <= 1) {
            // Solo queda un jugador o ninguno
            if (activePlayers.size() == 1 && activePlayers.get(0).getId() == actionPlayerId) {
                // El único activo soy yo, sigo jugando
                gameState.setCurrentPlayerIndex(getPlayerIndex(actionPlayerId));
                notifyTurnChange();
                return;
            }
            endRound();
            return;
        }
        
        // Pasar al siguiente jugador activo
        advanceTurn();
    }
    
    private int getPlayerIndex(int playerId) {
        List<Player> players = gameState.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == playerId) return i;
        }
        return 0;
    }
    
    public void playerStand(int playerId) {
        if (waitingForActionTarget) return;
        
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) return;
        if (!player.isActive()) return;
        if (player.getNumberCardCount() == 0 && player.getModifierCards().isEmpty()) return;
        
        player.setStanding(true);
        notifyPlayerStand(playerId);
        
        if (gameState.isRoundOver()) {
            endRound();
            return;
        }
        
        advanceTurn();
        notifyGameStateUpdate();
    }
    
    private void handleBust(Player player, Card bustCard) {
        if (player.hasSecondChance()) {
            deck.discard(player.useSecondChance());
            deck.discard(bustCard);
            notifyCardDealt(player.getId(), bustCard);
            return;
        }
        
        player.getNumberCards().add(bustCard);
        notifyCardDealt(player.getId(), bustCard);
        player.setBusted(true);
        notifyPlayerBusted(player.getId(), bustCard);
        
        if (gameState.isRoundOver()) endRound();
    }
    
    private void advanceTurn() {
        int next = getNextActivePlayerIndex(gameState.getCurrentPlayerIndex());
        if (next == -1) {
            endRound();
            return;
        }
        gameState.setCurrentPlayerIndex(next);
        notifyTurnChange();
    }
    
    private int getNextActivePlayerIndex(int current) {
        List<Player> players = gameState.getPlayers();
        for (int i = 1; i <= players.size(); i++) {
            int idx = (current + i) % players.size();
            if (players.get(idx).isActive()) return idx;
        }
        return -1;
    }
    
    private void endRound() {
        gameState.setPhase(GameState.Phase.ROUND_END);
        
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        isProcessingFlip3 = false;
        flip3PlayerID = -1;
        flip3CardsRemaining = 0;
        
        for (Player p : gameState.getPlayers()) {
            int roundScore = p.calculateRoundScore();
            p.addToTotalScore(roundScore);
        }
        
        int nextDealerIndex = findPlayerWithMostRoundPoints();
        if (nextDealerIndex != -1) {
            gameState.setDealerIndex(nextDealerIndex);
        } else {
            gameState.setDealerIndex((gameState.getDealerIndex() + 1) % gameState.getPlayers().size());
        }
        
        for (Player p : gameState.getPlayers()) {
            deck.discardAll(p.getAllCards());
        }
        
        notifyRoundEnd();
        
        if (gameState.isGameOver()) {
            endGame();
            return;
        }
        
        gameState.setRoundNumber(gameState.getRoundNumber() + 1);
    }
    
    private int findPlayerWithMostRoundPoints() {
        List<Player> players = gameState.getPlayers();
        int maxPoints = -1;
        int winnerIndex = -1;
        
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.isConnected()) {
                int roundScore = p.getRoundScore();
                if (roundScore > maxPoints) {
                    maxPoints = roundScore;
                    winnerIndex = i;
                }
            }
        }
        
        return winnerIndex;
    }
    
    public void startNextRound() {
        if (gameState.isGameOver()) {
            endGame();
            return;
        }
        startRound();
    }
    
    private void endGame() {
        gameState.setPhase(GameState.Phase.GAME_END);
        notifyGameEnd(gameState.getWinner());
    }
    
    private void updateDeckInfo() {
        gameState.setDeckSize(deck.getRemainingCards());
    }
    
    private void notifyCardDealt(int id, Card c) { for (GameEventListener l : listeners) l.onCardDealt(id, c); }
    private void notifyPlayerBusted(int id, Card c) { for (GameEventListener l : listeners) l.onPlayerBusted(id, c); }
    private void notifyPlayerStand(int id) { for (GameEventListener l : listeners) l.onPlayerStand(id); }
    private void notifyPlayerFrozen(int id) { for (GameEventListener l : listeners) l.onPlayerFrozen(id); }
    private void notifyActionCardDrawn(int id, Card c) { for (GameEventListener l : listeners) l.onActionCardDrawn(id, c); }
    private void notifyRoundEnd() { for (GameEventListener l : listeners) l.onRoundEnd(gameState.getPlayers(), gameState.getRoundNumber()); }
    private void notifyGameEnd(Player w) { for (GameEventListener l : listeners) l.onGameEnd(w); }
    private void notifyTurnChange() { Player c = gameState.getCurrentPlayer(); if (c != null) for (GameEventListener l : listeners) l.onTurnChange(c.getId()); }
    private void notifyGameStateUpdate() { for (GameEventListener l : listeners) l.onGameStateUpdate(gameState); }
    private void notifyNeedActionTarget(int id, Card c, List<Player> a) { for (GameEventListener l : listeners) l.onNeedActionTarget(id, c, a); }
    
    public GameState getGameState() { return gameState; }
}