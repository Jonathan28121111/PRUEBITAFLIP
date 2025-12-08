package flip7.client.gui;

import flip7.common.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameInfoPanel extends JPanel {
    private JLabel roundLabel, phaseLabel, deckLabel;
    private JTextArea logArea;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color ORANGE = new Color(251, 146, 60);
    
    public GameInfoPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        
        // Panel de info
        JPanel infoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 14, 14));
                
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 14, 14));
                
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-5, getHeight()-5, 13, 13));
            }
        };
        infoBox.setLayout(new GridLayout(3, 1, 0, 8));
        infoBox.setBorder(new EmptyBorder(15, 18, 15, 18));
        infoBox.setOpaque(false);
        
        roundLabel = createInfoLabel("RONDA", "1");
        phaseLabel = createInfoLabel("FASE", "Esperando");
        deckLabel = createInfoLabel("MAZO", "-- cartas");
        
        infoBox.add(roundLabel);
        infoBox.add(phaseLabel);
        infoBox.add(deckLabel);
        
        // Log area
        JPanel logBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 14, 14));
                
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 14, 14));
                
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-5, getHeight()-5, 13, 13));
            }
        };
        logBox.setOpaque(false);
        logBox.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        JLabel logTitle = new JLabel("📋 REGISTRO");
        logTitle.setFont(new Font("Arial", Font.BOLD, 11));
        logTitle.setForeground(BLUE_DARK);
        logTitle.setBorder(new EmptyBorder(0, 5, 8, 0));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setOpaque(false);
        logArea.setForeground(new Color(51, 65, 85));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(220, 140));
        
        logBox.add(logTitle, BorderLayout.NORTH);
        logBox.add(scroll, BorderLayout.CENTER);
        
        add(infoBox, BorderLayout.NORTH);
        add(logBox, BorderLayout.CENTER);
    }
    
    private JLabel createInfoLabel(String title, String value) {
        return new JLabel("<html><span style='color:#64748b;font-size:9px;font-weight:bold;'>" + title + "</span><br><span style='color:#1e293b;font-size:15px;font-weight:bold;'>" + value + "</span></html>");
    }
    
    private void updateLabel(JLabel label, String title, String value, String color) {
        label.setText("<html><span style='color:#64748b;font-size:9px;font-weight:bold;'>" + title + "</span><br><span style='color:" + color + ";font-size:15px;font-weight:bold;'>" + value + "</span></html>");
    }
    
    public void updateGameState(GameState s) {
        if (s == null) return;
        
        updateLabel(roundLabel, "RONDA", String.valueOf(s.getRoundNumber()), "#1e293b");
        
        String phase = "?";
        String phaseColor = "#1e293b";
        switch (s.getPhase()) {
            case WAITING_FOR_PLAYERS: phase = "Esperando"; phaseColor = "#f59e0b"; break;
            case DEALING: phase = "Repartiendo"; phaseColor = "#3b82f6"; break;
            case PLAYING: phase = "En juego"; phaseColor = "#22c55e"; break;
            case ROUND_END: phase = "Fin ronda"; phaseColor = "#f97316"; break;
            case GAME_END: phase = "Fin juego"; phaseColor = "#8b5cf6"; break;
        }
        updateLabel(phaseLabel, "FASE", phase, phaseColor);
        updateLabel(deckLabel, "MAZO", s.getDeckSize() + " cartas", "#64748b");
    }
    
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void showScoreboard(java.util.List<Player> players) {
        StringBuilder sb = new StringBuilder("\n═══ PUNTUACIONES ═══\n");
        players.sort((a, b) -> b.getTotalScore() - a.getTotalScore());
        int pos = 1;
        for (Player p : players) {
            String medal = pos == 1 ? "🥇" : pos == 2 ? "🥈" : pos == 3 ? "🥉" : "  ";
            sb.append(medal + " " + p.getName() + ": " + p.getTotalScore() + "\n");
            pos++;
        }
        log(sb.toString());
    }
}
