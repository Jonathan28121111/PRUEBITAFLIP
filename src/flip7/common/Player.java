package flip7.common;
import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name; private int id;
    private List<Card> numberCards = new ArrayList<>(), modifierCards = new ArrayList<>(), actionCards = new ArrayList<>();
    private Card secondChanceCard;
    private int totalScore, roundScore;
    private boolean isBusted, isStanding, isFrozen, isConnected = true;
    
    public Player(String name, int id) { this.name = name; this.id = id; }
    public void resetForNewRound() { numberCards.clear(); modifierCards.clear(); actionCards.clear(); secondChanceCard = null; roundScore = 0; isBusted = isStanding = isFrozen = false; }
    public void resetForNewGame() { resetForNewRound(); totalScore = 0; }
    
    public boolean addCard(Card c) {
        if (c == null) return false;
        switch (c.getType()) {
            case NUMBER: if (hasNumber(c.getValue())) return false; numberCards.add(c); return true;
            case MODIFIER: modifierCards.add(c); return true;
            case SECOND_CHANCE: if (secondChanceCard == null) secondChanceCard = c; return true;
            case FREEZE: actionCards.add(c); return true;
            case FLIP_THREE: actionCards.add(c); return true;
            default: return true;
        }
    }
    
    public void addActionCard(Card c) { if (c != null) actionCards.add(c); }
    
    public boolean hasNumber(int n) { for (Card c : numberCards) if (c.getValue() == n) return true; return false; }
    public boolean hasSecondChance() { return secondChanceCard != null; }
    public Card useSecondChance() { Card c = secondChanceCard; secondChanceCard = null; return c; }
    public void setSecondChanceCard(Card c) { secondChanceCard = c; }
    
    public int calculateRoundScore() {
        if (isBusted) { roundScore = 0; return 0; }
        int total = 0; for (Card c : numberCards) total += c.getValue();
        boolean hasX2 = false; int modTotal = 0;
        for (Card c : modifierCards) { if (c.isX2()) hasX2 = true; else modTotal += c.getValue(); }
        if (hasX2) total *= 2;
        roundScore = total + modTotal;
        if (numberCards.size() >= 7) roundScore += 15;
        return roundScore;
    }
    
    public boolean hasFlip7() { return numberCards.size() >= 7; }
    public boolean isActive() { return !isBusted && !isStanding && !isFrozen && isConnected; }
    public String getName() { return name; }
    public int getId() { return id; }
    public int getTotalScore() { return totalScore; }
    public void addToTotalScore(int p) { totalScore += p; }
    public int getRoundScore() { return roundScore; }
    public boolean isBusted() { return isBusted; }
    public void setBusted(boolean b) { isBusted = b; }
    public boolean isStanding() { return isStanding; }
    public void setStanding(boolean s) { isStanding = s; }
    public boolean isFrozen() { return isFrozen; }
    public void setFrozen(boolean f) { isFrozen = f; if (f) isStanding = true; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean c) { isConnected = c; }
    public int getNumberCardCount() { return numberCards.size(); }
    public List<Card> getNumberCards() { return numberCards; }
    public List<Card> getModifierCards() { return modifierCards; }
    public List<Card> getActionCards() { return actionCards; }
    public List<Card> getAllCards() { 
        List<Card> all = new ArrayList<>(); 
        // Primero cartas de poder/acción (arriba)
        all.addAll(actionCards); 
        if (secondChanceCard != null) all.add(secondChanceCard); 
        all.addAll(modifierCards);
        // Después cartas numéricas (abajo)
        all.addAll(numberCards); 
        return all; 
    }
}
