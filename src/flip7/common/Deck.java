package flip7.common;
import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Card> cards = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    
    public Deck() { initializeDeck(); }
    
    private void initializeDeck() {
        cards.add(new Card(Card.CardType.NUMBER, 0));
        for (int n = 1; n <= 12; n++) for (int i = 0; i < n; i++) cards.add(new Card(Card.CardType.NUMBER, n));
        cards.add(new Card(Card.CardType.MODIFIER, 2));
        cards.add(new Card(Card.CardType.MODIFIER, 4));
        cards.add(new Card(Card.CardType.MODIFIER, 6));
        cards.add(new Card(Card.CardType.MODIFIER, 8));
        cards.add(new Card(Card.CardType.MODIFIER, 8));
        cards.add(new Card(Card.CardType.MODIFIER, 10));
        cards.add(new Card(Card.CardType.MODIFIER, -1));
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(Card.CardType.FREEZE, 0));
            cards.add(new Card(Card.CardType.FLIP_THREE, 0));
            cards.add(new Card(Card.CardType.SECOND_CHANCE, 0));
        }
        shuffle();
    }
    
    public void shuffle() { Collections.shuffle(cards); }
    public Card drawCard() { if (cards.isEmpty()) reshuffleDiscardPile(); return cards.isEmpty() ? null : cards.remove(0); }
    public void discard(Card c) { if (c != null) discardPile.add(c); }
    public void discardAll(List<Card> c) { discardPile.addAll(c); }
    private void reshuffleDiscardPile() { if (!discardPile.isEmpty()) { cards.addAll(discardPile); discardPile.clear(); shuffle(); } }
    public int getRemainingCards() { return cards.size(); }
    public void reset() { cards.clear(); discardPile.clear(); initializeDeck(); }
}
