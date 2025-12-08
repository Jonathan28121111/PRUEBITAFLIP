package flip7.game;

import flip7.common.*;
import java.util.*;

public class GameLogic {
    private GameState gameState = new GameState();
    private Deck deck = new Deck();
    private List<GameEventListener> listeners = new ArrayList<>();
    
    // ✅ NUEVOS: Control de cartas de acción
    private boolean waitingForActionTarget = false;
    private int playerWaitingForAction = -1;
    private Card pendingActionCard = null;
    private Set<Integer> playersWhoReceivedSecondChance = new HashSet<>(); // Por ronda
    
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
        
        // ✅ RESETEAR control de Second Chance por ronda
        playersWhoReceivedSecondChance.clear();
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        
        dealInitialCards();
        
        gameState.setPhase(GameState.Phase.PLAYING);
        
        int startIdx = (gameState.getDealerIndex() + 1) % gameState.getPlayers().size();
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
                if (card.isActionCard()) {
                    deck.discard(card);
                    card = deck.drawCard();
                }
                if (card != null) {
                    player.addCard(card);
                    notifyCardDealt(player.getId(), card);
                }
            }
        }
        updateDeckInfo();
    }
    
    public void playerHit(int playerId) {
        // ✅ BLOQUEAR si se está esperando selección de objetivo
        if (waitingForActionTarget) {
            return;
        }
        
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) {
            return;
        }
        
        if (!player.isActive()) {
            return;
        }
        
        Card card = deck.drawCard();
        if (card == null) return;
        
        processDealtCard(player, card);
        
        if (player.hasFlip7() || gameState.isRoundOver()) {
            endRound();
            return;
        }
        
        // ✅ SOLO pasar turno si NO se está esperando acción
        if (!waitingForActionTarget) {
            advanceTurn();
            notifyGameStateUpdate();
        }
    }
    
    private void processDealtCard(Player player, Card card) {
        if (card.isActionCard()) {
            notifyActionCardDrawn(player.getId(), card);
            
            // ✅ CASO ESPECIAL: Second Chance
            if (card.getType() == Card.CardType.SECOND_CHANCE) {
                // Si el jugador NO tiene Second Chance, dárselo automáticamente
                if (!playersWhoReceivedSecondChance.contains(player.getId())) {
                    applyActionCard(player.getId(), card);
                    return; // Terminar aquí, no pedir selección
                }
                // Si YA tiene uno, dar opción de regalar
            }
            
            List<Player> active = gameState.getActivePlayers();
            
            // ✅ Filtrar jugadores que ya tienen Second Chance
            if (card.getType() == Card.CardType.SECOND_CHANCE) {
                active = filterPlayersWithoutSecondChance(active);
            }
            
            if (active.isEmpty()) {
                // Nadie puede recibir la carta, descartarla
                deck.discard(card);
                return;
            }
            
            if (active.size() == 1 && active.get(0).getId() == player.getId()) {
                // Solo puede asignársela a sí mismo
                applyActionCard(player.getId(), card);
            } else {
                // ✅ BLOQUEAR el juego hasta que elija
                waitingForActionTarget = true;
                playerWaitingForAction = player.getId();
                pendingActionCard = card;
                notifyNeedActionTarget(player.getId(), card, active);
            }
        } else {
            boolean success = player.addCard(card);
            if (!success && card.isNumberCard()) {
                handleBust(player, card);
            } else {
                notifyCardDealt(player.getId(), card);
            }
        }
        updateDeckInfo();
    }
    
    // ✅ NUEVO: Filtrar jugadores sin Second Chance
    private List<Player> filterPlayersWithoutSecondChance(List<Player> players) {
        List<Player> filtered = new ArrayList<>();
        for (Player p : players) {
            if (!playersWhoReceivedSecondChance.contains(p.getId())) {
                filtered.add(p);
            }
        }
        return filtered;
    }
    
    public void assignActionCard(int fromId, int targetId, Card card) {
        // ✅ VALIDAR que quien asigna es quien debe elegir
        if (!waitingForActionTarget || playerWaitingForAction != fromId) {
            return;
        }
        
        Player target = gameState.getPlayerById(targetId);
        if (target == null || !target.isActive()) {
            return;
        }
        
        // ✅ VALIDAR Second Chance duplicado
        if (card.getType() == Card.CardType.SECOND_CHANCE) {
            if (playersWhoReceivedSecondChance.contains(targetId)) {
                // No puede recibir otro Second Chance
                return;
            }
        }
        
        applyActionCard(targetId, card);
        
        // ✅ DESBLOQUEAR y pasar turno
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        
        advanceTurn();
        notifyGameStateUpdate();
    }
    
    private void applyActionCard(int targetId, Card card) {
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
                handleFlipThree(target);
                break;
                
            case SECOND_CHANCE:
                // ✅ REGISTRAR que este jugador ya recibió Second Chance
                playersWhoReceivedSecondChance.add(targetId);
                target.setSecondChanceCard(card);
                notifyCardDealt(targetId, card);
                break;
        }
    }
    
    private void handleFlipThree(Player player) {
        List<Card> drawnCards = new ArrayList<>();
        
        for (int i = 0; i < 3 && !player.isBusted() && !player.hasFlip7(); i++) {
            Card card = deck.drawCard();
            if (card == null) break;
            
            drawnCards.add(card);
            
            if (card.isActionCard()) {
                if (card.getType() == Card.CardType.FREEZE) {
                    player.addActionCard(card);
                    notifyCardDealt(player.getId(), card);
                    player.setFrozen(true);
                    notifyPlayerFrozen(player.getId());
                } else if (card.getType() == Card.CardType.SECOND_CHANCE) {
                    // ✅ AUTOMÁTICO: Solo dar si no tiene uno
                    if (!playersWhoReceivedSecondChance.contains(player.getId())) {
                        playersWhoReceivedSecondChance.add(player.getId());
                        player.setSecondChanceCard(card);
                        notifyCardDealt(player.getId(), card);
                    } else {
                        // Ya tiene uno, descartar
                        deck.discard(card);
                    }
                } else {
                    deck.discard(card);
                }
            } else {
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
        
        if (player.hasFlip7()) endRound();
        updateDeckInfo();
    }
    
    public void playerStand(int playerId) {
        // ✅ BLOQUEAR si se está esperando selección de objetivo
        if (waitingForActionTarget) {
            return;
        }
        
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) {
            return;
        }
        
        if (!player.isActive()) {
            return;
        }
        
        if (player.getNumberCardCount() == 0 && player.getModifierCards().isEmpty()) {
            return;
        }
        
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
        
        // ✅ LIMPIAR estado de espera
        waitingForActionTarget = false;
        playerWaitingForAction = -1;
        pendingActionCard = null;
        
        for (Player p : gameState.getPlayers()) {
            p.addToTotalScore(p.calculateRoundScore());
        }
        
        for (Player p : gameState.getPlayers()) {
            deck.discardAll(p.getAllCards());
        }
        
        notifyRoundEnd();
        
        if (gameState.isGameOver()) {
            endGame();
            return;
        }
        
        // ✅ NUEVO: El siguiente dealer es quien hizo MÁS puntos en esta ronda
        int nextDealerIndex = findPlayerWithMostRoundPoints();
        if (nextDealerIndex != -1) {
            gameState.setDealerIndex(nextDealerIndex);
        } else {
            // Fallback: rotar normal si no se puede determinar
            gameState.setDealerIndex((gameState.getDealerIndex() + 1) % gameState.getPlayers().size());
        }
        
        gameState.setRoundNumber(gameState.getRoundNumber() + 1);
    }
    
    /**
     * ✅ NUEVO: Encuentra al jugador que hizo más puntos en la ronda actual
     */
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
    
    // Notificaciones
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