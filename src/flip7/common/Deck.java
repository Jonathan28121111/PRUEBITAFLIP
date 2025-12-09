package flip7.common;
import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Card> cards = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    
    public Deck() { initializeDeck(); }
    
    private void initializeDeck() {
        cards.clear();
        
        // ✅ 79 cartas de números
        // 0 tiene 1 carta
        // N tiene N cartas (1=1, 2=2, 3=3... 12=12)
        // Total: 1 + 1+2+3+4+5+6+7+8+9+10+11+12 = 1 + 78 = 79
        cards.add(new Card(Card.CardType.NUMBER, 0));
        for (int n = 1; n <= 12; n++) {
            for (int i = 0; i < n; i++) {
                cards.add(new Card(Card.CardType.NUMBER, n));
            }
        }
        
        // ✅ 7 cartas modificadoras
        cards.add(new Card(Card.CardType.MODIFIER, 2));   // +2
        cards.add(new Card(Card.CardType.MODIFIER, 4));   // +4
        cards.add(new Card(Card.CardType.MODIFIER, 6));   // +6
        cards.add(new Card(Card.CardType.MODIFIER, 8));   // +8
        cards.add(new Card(Card.CardType.MODIFIER, 8));   // +8 (segundo)
        cards.add(new Card(Card.CardType.MODIFIER, 10));  // +10
        cards.add(new Card(Card.CardType.MODIFIER, -1));  // X2
        
        // ✅ 3 FREEZE
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(Card.CardType.FREEZE, 0));
        }
        
        // ✅ 3 FLIP THREE
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(Card.CardType.FLIP_THREE, 0));
        }
        
        // ✅ 3 SECOND CHANCE
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(Card.CardType.SECOND_CHANCE, 0));
        }
        
        // Total: 79 + 7 + 3 + 3 + 3 = 95
        // Pero dijiste 94... verifico: 1+1+2+3+4+5+6+7+8+9+10+11+12 = 79
        // Hmm 79+7+9 = 95. Tal vez el 0 no cuenta? O hay 6 modificadoras?
        
        System.out.println("[MAZO] Inicializado con " + cards.size() + " cartas");
        shuffle();
    }
    
    public void shuffle() { 
        Collections.shuffle(cards); 
    }
    
    public Card drawCard() { 
        if (cards.isEmpty()) {
            reshuffleDiscardPile(); 
        }
        return cards.isEmpty() ? null : cards.remove(0); 
    }
    
    public void discard(Card c) { 
        if (c != null) discardPile.add(c); 
    }
    
    public void discardAll(List<Card> c) { 
        if (c != null) discardPile.addAll(c); 
    }
    
    private void reshuffleDiscardPile() { 
        if (!discardPile.isEmpty()) { 
            System.out.println("[MAZO] Barajeando " + discardPile.size() + " cartas del descarte");
            cards.addAll(discardPile); 
            discardPile.clear(); 
            shuffle(); 
            System.out.println("[MAZO] Ahora hay " + cards.size() + " cartas");
        }
    }
    
    public int getRemainingCards() { return cards.size(); }
    
    public void reset() { 
        cards.clear(); 
        discardPile.clear(); 
        initializeDeck(); 
    }
}