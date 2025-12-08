package flip7.common;
import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Phase { WAITING_FOR_PLAYERS, DEALING, PLAYING, ROUND_END, GAME_END }
    
    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex, dealerIndex, roundNumber = 1, deckSize, winningScore = 200;
    private Phase phase = Phase.WAITING_FOR_PLAYERS;
    
    public List<Player> getPlayers() { return players; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int i) { currentPlayerIndex = i; }
    public int getDealerIndex() { return dealerIndex; }
    public void setDealerIndex(int i) { dealerIndex = i; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int r) { roundNumber = r; }
    public Phase getPhase() { return phase; }
    public void setPhase(Phase p) { phase = p; }
    public int getDeckSize() { return deckSize; }
    public void setDeckSize(int s) { deckSize = s; }
    public int getWinningScore() { return winningScore; }
    
    public Player getCurrentPlayer() { return (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) ? players.get(currentPlayerIndex) : null; }
    public Player getPlayerById(int id) { for (Player p : players) if (p.getId() == id) return p; return null; }
    public List<Player> getActivePlayers() { List<Player> a = new ArrayList<>(); for (Player p : players) if (p.isActive()) a.add(p); return a; }
    public boolean isRoundOver() { for (Player p : players) if (p.isActive()) return false; return true; }
    public boolean isGameOver() { for (Player p : players) if (p.getTotalScore() >= winningScore) return true; return false; }
    public Player getWinner() { Player w = null; int max = -1; for (Player p : players) if (p.getTotalScore() > max) { max = p.getTotalScore(); w = p; } return w; }
}
