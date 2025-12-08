package flip7.game;

import flip7.common.*;
import java.util.*;

public class GameLogic {
    private GameState gameState = new GameState();
    private Deck deck = new Deck();
    private List<GameEventListener> listeners = new ArrayList<>();
    
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
        
        // Repartir UNA carta inicial a cada jugador
        dealInitialCards();
        
        gameState.setPhase(GameState.Phase.PLAYING);
        
        // El primer turno es del jugador despues del dealer
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
                // Para carta inicial, solo numeros o modificadores van directo
                if (card.isActionCard()) {
                    // Descartar y sacar otra
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
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        // Verificar que es el turno de este jugador
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) {
            return;
        }
        
        if (!player.isActive()) {
            return;
        }
        
        // Sacar UNA carta
        Card card = deck.drawCard();
        if (card == null) return;
        
        processDealtCard(player, card);
        
        // Verificar si termino la ronda
        if (player.hasFlip7() || gameState.isRoundOver()) {
            endRound();
            return;
        }
        
        // IMPORTANTE: Pasar al siguiente jugador despues de UNA carta
        advanceTurn();
        notifyGameStateUpdate();
    }
    
    private void processDealtCard(Player player, Card card) {
        if (card.isActionCard()) {
            notifyActionCardDrawn(player.getId(), card);
            List<Player> active = gameState.getActivePlayers();
            
            if (active.size() == 1 && active.get(0).getId() == player.getId()) {
                applyActionCard(player.getId(), card);
            } else {
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
    
    public void assignActionCard(int fromId, int targetId, Card card) {
        Player target = gameState.getPlayerById(targetId);
        if (target != null && target.isActive()) {
            applyActionCard(targetId, card);
            
            // Despues de asignar carta de accion, pasar turno
            advanceTurn();
            notifyGameStateUpdate();
        }
    }
    
    private void applyActionCard(int targetId, Card card) {
        Player target = gameState.getPlayerById(targetId);
        if (target == null) return;
        
        switch (card.getType()) {
            case FREEZE:
                target.addActionCard(card);  // Agregar carta al jugador para mostrarla
                target.setFrozen(true);
                notifyCardDealt(targetId, card);  // Notificar que recibió la carta
                notifyPlayerFrozen(targetId);
                break;
            case FLIP_THREE:
                target.addActionCard(card);  // Agregar carta al jugador para mostrarla
                notifyCardDealt(targetId, card);
                handleFlipThree(target);
                break;
            case SECOND_CHANCE:
                if (!target.hasSecondChance()) {
                    target.setSecondChanceCard(card);
                    notifyCardDealt(targetId, card);
                } else {
                    deck.discard(card);
                }
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
                } else if (card.getType() == Card.CardType.SECOND_CHANCE && !player.hasSecondChance()) {
                    player.setSecondChanceCard(card);
                    notifyCardDealt(player.getId(), card);
                } else {
                    deck.discard(card);
                }
            } else {
                boolean success = player.addCard(card);
                notifyCardDealt(player.getId(), card);
                if (!success && card.isNumberCard()) {
                    // Carta duplicada - bust
                    player.getNumberCards().add(card);  // Agregar para mostrar
                    player.setBusted(true);
                    notifyPlayerBusted(player.getId(), card);
                    break;
                }
            }
            
            // Pequeña pausa para que se vea cada carta (el cliente debería manejar esto)
            notifyGameStateUpdate();
        }
        
        if (player.hasFlip7()) endRound();
        updateDeckInfo();
    }
    
    public void playerStand(int playerId) {
        Player player = gameState.getPlayerById(playerId);
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (player == null || currentPlayer == null || currentPlayer.getId() != playerId) {
            return;
        }
        
        if (!player.isActive()) {
            return;
        }
        
        // Necesita al menos una carta para plantarse
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
            notifyCardDealt(player.getId(), bustCard); // Mostrar la carta que salio
            return;
        }
        
        // Agregar la carta duplicada para mostrarla (aunque cause bust)
        player.getNumberCards().add(bustCard);
        notifyCardDealt(player.getId(), bustCard);  // Mostrar la carta que causó el bust
        
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
        
        gameState.setRoundNumber(gameState.getRoundNumber() + 1);
        gameState.setDealerIndex((gameState.getDealerIndex() + 1) % gameState.getPlayers().size());
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
