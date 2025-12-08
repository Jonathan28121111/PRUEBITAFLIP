package flip7.common;
import java.io.Serializable;
import java.awt.Color;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum CardType { NUMBER, MODIFIER, FREEZE, FLIP_THREE, SECOND_CHANCE }
    private CardType type; private int value; private boolean isX2;
    
    public Card(CardType type, int value) { this.type = type; this.value = value; this.isX2 = (type == CardType.MODIFIER && value == -1); }
    public CardType getType() { return type; }
    public int getValue() { return value; }
    public boolean isX2() { return isX2; }
    public boolean isActionCard() { return type == CardType.FREEZE || type == CardType.FLIP_THREE || type == CardType.SECOND_CHANCE; }
    public boolean isNumberCard() { return type == CardType.NUMBER; }
    public boolean isModifierCard() { return type == CardType.MODIFIER; }
    
    public String toString() {
        switch (type) {
            case NUMBER: return "Numero " + value;
            case MODIFIER: return isX2 ? "X2" : "+" + value;
            case FREEZE: return "CONGELAR";
            case FLIP_THREE: return "VOLTEAR 3";
            case SECOND_CHANCE: return "2a OPORTUNIDAD";
            default: return "?";
        }
    }
    
    public String getDisplayName() {
        switch (type) {
            case NUMBER: return String.valueOf(value);
            case MODIFIER: return isX2 ? "X2" : "+" + value;
            case FREEZE: return "FRZ"; case FLIP_THREE: return "FL3"; case SECOND_CHANCE: return "2ND";
            default: return "?";
        }
    }
    
    public Color getCardColor() {
        switch (type) {
            case NUMBER: return new Color(55, 120, 180);
            case MODIFIER: return isX2 ? new Color(200, 160, 40) : new Color(45, 160, 90);
            case FREEZE: return new Color(60, 150, 200);
            case FLIP_THREE: return new Color(200, 120, 50);
            case SECOND_CHANCE: return new Color(130, 90, 180);
            default: return Color.GRAY;
        }
    }
}
