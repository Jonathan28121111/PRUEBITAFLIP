package flip7.client.gui;

import flip7.common.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class PlayerPanel extends JPanel {
    private Player player;
    private boolean isMyTurn, isLocalPlayer;
    private CardPanel cardPanel;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color BLUE_LIGHT = new Color(135, 206, 250);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color ORANGE = new Color(251, 146, 60);
    private static final Color RED = new Color(248, 113, 113);
    private static final Color CYAN = new Color(103, 232, 249);
    
    public PlayerPanel(boolean isLocalPlayer) {
        this.isLocalPlayer = isLocalPlayer;
        setLayout(new BorderLayout());
        setOpaque(false);
        
        cardPanel = new CardPanel();
        add(cardPanel, BorderLayout.CENTER);
    }
    
    public void setPlayer(Player p) {
        player = p;
        if (player != null) {
            cardPanel.setCards(player.getAllCards());
        } else {
            cardPanel.clear();
        }
        repaint();
    }
    
    public void setCurrentTurn(boolean t) {
        isMyTurn = t;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth(), h = getHeight();
        
        // Sombra
        g2.setColor(new Color(100, 150, 200, 40));
        g2.fill(new RoundRectangle2D.Float(4, 4, w - 4, h - 4, 16, 16));
        
        // Fondo
        Color bgColor = isLocalPlayer ? new Color(240, 253, 244) : new Color(255, 255, 255);
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 4, 16, 16));
        
        // Borde
        Color borderColor = isLocalPlayer ? GREEN : new Color(203, 213, 225);
        if (isMyTurn && player != null && player.isActive()) {
            borderColor = BLUE_DARK;
            // Glow
            g2.setColor(new Color(66, 133, 244, 40));
            g2.setStroke(new BasicStroke(8f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 4, h - 4, 16, 16));
        }
        
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(borderColor);
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 6, h - 6, 15, 15));
        
        if (player != null) {
            drawPlayerHeader(g2, w);
            drawPlayerStatus(g2, w, h);
        } else {
            g2.setFont(new Font("Arial", Font.ITALIC, 13));
            g2.setColor(new Color(148, 163, 184));
            String text = "Esperando...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
        }
        
        super.paintComponent(g);
    }
    
    private void drawPlayerHeader(Graphics2D g2, int w) {
        // Barra superior
        g2.setColor(isLocalPlayer ? new Color(220, 252, 231) : new Color(241, 245, 249));
        g2.fill(new RoundRectangle2D.Float(3, 3, w - 10, 38, 13, 13));
        g2.fillRect(3, 25, w - 10, 16);
        
        // Indicador
        Color indicatorColor;
        if (player.isBusted()) indicatorColor = RED;
        else if (player.isFrozen()) indicatorColor = CYAN;
        else if (player.isStanding()) indicatorColor = GREEN;
        else if (isMyTurn && player.isActive()) indicatorColor = BLUE_DARK;
        else indicatorColor = new Color(148, 163, 184);
        
        g2.setColor(indicatorColor);
        g2.fillOval(12, 12, 14, 14);
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillOval(14, 14, 6, 6);
        
        // Nombre
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(30, 41, 59));
        String name = player.getName() + (isLocalPlayer ? " (Tú)" : "");
        g2.drawString(name, 32, 24);
        
        // Puntuacion
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String score = player.getTotalScore() + "";
        FontMetrics fm = g2.getFontMetrics();
        
        g2.setColor(BLUE_DARK);
        g2.drawString(score, w - fm.stringWidth(score) - 15, 26);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(100, 116, 139));
        g2.drawString("pts", w - 12 - fm.stringWidth(score) + fm.stringWidth(score) + 3, 26);
    }
    
    private void drawPlayerStatus(Graphics2D g2, int w, int h) {
        String status = null;
        Color statusColor = null;
        Color bgColor = null;
        
        if (player.isBusted()) {
            status = "ELIMINADO";
            statusColor = RED;
            bgColor = new Color(254, 226, 226);
        } else if (player.isFrozen()) {
            status = "CONGELADO";
            statusColor = new Color(6, 182, 212);
            bgColor = new Color(207, 250, 254);
        } else if (player.isStanding()) {
            status = "PLANTADO";
            statusColor = new Color(22, 163, 74);
            bgColor = new Color(220, 252, 231);
        } else if (isMyTurn && player.isActive()) {
            status = isLocalPlayer ? "► ¡TU TURNO!" : "► JUGANDO";
            statusColor = BLUE_DARK;
            bgColor = new Color(219, 234, 254);
        }
        
        if (status != null) {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int statusW = fm.stringWidth(status) + 20;
            int statusX = (w - statusW) / 2;
            
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(statusX, h - 30, statusW, 22, 11, 11));
            
            g2.setColor(statusColor);
            g2.drawString(status, statusX + 10, h - 14);
        }
    }
    
    @Override
    public Insets getInsets() {
        return new Insets(45, 10, 35, 10);
    }
}
