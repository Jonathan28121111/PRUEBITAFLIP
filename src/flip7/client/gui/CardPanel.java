package flip7.client.gui;

import flip7.common.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class CardPanel extends JPanel {
    private java.util.List<Card> cards = new ArrayList<>();
    
    public CardPanel() {
        setOpaque(false);
    }
    
    public void setCards(java.util.List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        repaint();
    }
    
    public void clear() {
        cards.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int cardW = 58, cardH = 85;
        int spacing = 6;
        int y = 5;
        
        if (cards.isEmpty()) {
            // Carta vacia placeholder
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fill(new RoundRectangle2D.Float(5, y, cardW, cardH, 10, 10));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));
            g2.setColor(new Color(255, 255, 255, 30));
            g2.draw(new RoundRectangle2D.Float(5, y, cardW, cardH, 10, 10));
            return;
        }
        
        // Calcular si necesitamos solapar cartas
        int totalWidth = cards.size() * cardW + (cards.size() - 1) * spacing;
        int availableWidth = getWidth() - 10;
        
        if (totalWidth > availableWidth && cards.size() > 1) {
            spacing = (availableWidth - cardW) / (cards.size()) - cardW + spacing;
            if (spacing < -cardW + 15) spacing = -cardW + 15;
        }
        
        int x = 5;
        for (int i = 0; i < cards.size(); i++) {
            drawCard(g2, cards.get(i), x, y, cardW, cardH);
            x += cardW + spacing;
        }
    }
    
    private void drawCard(Graphics2D g2, Card card, int x, int y, int w, int h) {
        Color bgColor, borderColor, textColor, accentColor;
        
        switch (card.getType()) {
            case NUMBER:
                bgColor = new Color(255, 252, 240);
                borderColor = new Color(180, 165, 100);
                accentColor = new Color(200, 185, 120);
                textColor = getNumberColor(card.getValue());
                break;
            case MODIFIER:
                bgColor = new Color(255, 220, 100);
                borderColor = new Color(200, 160, 50);
                accentColor = new Color(220, 180, 70);
                textColor = new Color(150, 100, 30);
                break;
            case FREEZE:
                bgColor = new Color(180, 220, 245);
                borderColor = new Color(100, 160, 200);
                accentColor = new Color(140, 190, 225);
                textColor = new Color(40, 100, 150);
                break;
            case FLIP_THREE:
                bgColor = new Color(255, 240, 120);
                borderColor = new Color(200, 180, 60);
                accentColor = new Color(230, 210, 90);
                textColor = new Color(50, 120, 100);
                break;
            case SECOND_CHANCE:
                bgColor = new Color(230, 100, 120);
                borderColor = new Color(180, 60, 80);
                accentColor = new Color(210, 80, 100);
                textColor = new Color(255, 250, 250);
                break;
            default:
                bgColor = Color.WHITE;
                borderColor = Color.GRAY;
                accentColor = Color.LIGHT_GRAY;
                textColor = Color.BLACK;
        }
        
        // Sombra
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Float(x + 3, y + 3, w, h, 10, 10));
        
        // Fondo principal
        GradientPaint bgGrad = new GradientPaint(x, y, bgColor, x, y + h, darker(bgColor, 0.95f));
        g2.setPaint(bgGrad);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
        
        // Marco decorativo exterior
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(borderColor);
        g2.draw(new RoundRectangle2D.Float(x + 4, y + 4, w - 8, h - 8, 6, 6));
        
        // Linea decorativa interior
        g2.setStroke(new BasicStroke(0.8f));
        g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 80));
        g2.draw(new RoundRectangle2D.Float(x + 7, y + 7, w - 14, h - 14, 4, 4));
        
        // Decoraciones de esquina (alas estilo art deco)
        drawCornerWings(g2, x, y, w, h, accentColor);
        
        // Texto principal
        String mainText = getCardText(card);
        g2.setColor(textColor);
        
        Font mainFont;
        if (card.getType() == Card.CardType.NUMBER) {
            mainFont = new Font("Georgia", Font.BOLD, 28);
        } else if (card.getType() == Card.CardType.MODIFIER) {
            mainFont = new Font("Georgia", Font.BOLD, 22);
        } else {
            mainFont = new Font("Arial", Font.BOLD, 11);
        }
        g2.setFont(mainFont);
        
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (w - fm.stringWidth(mainText)) / 2;
        int textY = y + h/2 + fm.getAscent()/3 - 2;
        
        // Sombra del texto
        g2.setColor(new Color(0, 0, 0, 25));
        g2.drawString(mainText, textX + 1, textY + 1);
        g2.setColor(textColor);
        g2.drawString(mainText, textX, textY);
        
        // Subtexto (nombre)
        String subText = getCardSubText(card);
        if (subText != null) {
            g2.setFont(new Font("Arial", Font.BOLD, 7));
            fm = g2.getFontMetrics();
            textX = x + (w - fm.stringWidth(subText)) / 2;
            g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 140));
            g2.drawString(subText, textX, y + h - 10);
        }
        
        // Numero pequeño en esquina
        if (card.getType() == Card.CardType.NUMBER) {
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180));
            g2.drawString(String.valueOf(card.getValue()), x + 10, y + 16);
        }
    }
    
    private void drawCornerWings(Graphics2D g2, int x, int y, int w, int h, Color color) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.setStroke(new BasicStroke(1.2f));
        
        // Alas superiores
        int wingY = y + 18;
        g2.drawArc(x + 12, wingY, 12, 8, 0, 180);
        g2.drawArc(x + w - 24, wingY, 12, 8, 0, 180);
    }
    
    private Color getNumberColor(int value) {
        switch (value) {
            case 0: return new Color(80, 80, 80);
            case 1: case 2: return new Color(110, 90, 60);
            case 3: case 6: return new Color(20, 130, 110);
            case 4: case 5: return new Color(170, 60, 90);
            case 7: case 8: return new Color(220, 140, 70);
            case 9: case 10: return new Color(120, 70, 140);
            case 11: case 12: return new Color(70, 110, 160);
            default: return Color.BLACK;
        }
    }
    
    private Color darker(Color c, float f) {
        return new Color((int)(c.getRed()*f), (int)(c.getGreen()*f), (int)(c.getBlue()*f));
    }
    
    private String getCardText(Card card) {
        switch (card.getType()) {
            case NUMBER: return String.valueOf(card.getValue());
            case MODIFIER: return card.isX2() ? "×2" : "+" + card.getValue();
            case FREEZE: return "FREEZE";
            case FLIP_THREE: return "FLIP 3";
            case SECOND_CHANCE: return "2ND";
            default: return "?";
        }
    }
    
    private String getCardSubText(Card card) {
        if (card.getType() == Card.CardType.NUMBER) {
            String[] n = {"CERO", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE"};
            return card.getValue() < n.length ? n[card.getValue()] : null;
        }
        if (card.getType() == Card.CardType.SECOND_CHANCE) return "OPORTUNIDAD";
        if (card.getType() == Card.CardType.FREEZE) return "CONGELAR";
        if (card.getType() == Card.CardType.FLIP_THREE) return "TRES";
        return null;
    }
}
