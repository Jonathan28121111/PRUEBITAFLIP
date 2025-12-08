package flip7.client.gui;

import flip7.common.GameRoom;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class WaitingRoomPanel extends JPanel {
    private JLabel roomNameLabel, playersCountLabel, statusLabel;
    private JPanel playerListPanel;
    private JButton readyBtn, leaveBtn;
    private WaitingRoomListener listener;
    private boolean isReady = false;
    private boolean isSpectator = false;
    private Set<String> readyPlayers = new HashSet<>();
    private int totalPlayers = 0;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color RED = new Color(248, 113, 113);
    private static final Color ORANGE = new Color(251, 146, 60);
    private static final Color PURPLE = new Color(139, 92, 246);
    
    private JButton joinAsPlayerBtn;
    private int maxPlayers = 4;
    
    public interface WaitingRoomListener {
        void onReady();
        void onLeaveRoom();
        void onJoinAsPlayer();
    }
    
    public WaitingRoomPanel(WaitingRoomListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 60, 30, 60));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        roomNameLabel = new JLabel("SALA DE ESPERA");
        roomNameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        roomNameLabel.setForeground(BLUE_DARK);
        
        playersCountLabel = new JLabel("Jugadores: 0/0");
        playersCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playersCountLabel.setForeground(new Color(100, 116, 139));
        
        header.add(roomNameLabel, BorderLayout.WEST);
        header.add(playersCountLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth(), getHeight(), 20, 20));
                
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(BLUE_DARK);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 19, 19));
            }
        };
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Jugadores en la sala:");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(51, 65, 85));
        
        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(playerListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        
        center.add(title, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Presiona LISTO para comenzar (minimo 2 jugadores)");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        center.add(statusLabel, BorderLayout.SOUTH);
        
        return center;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        leaveBtn = createButton("SALIR", RED);
        readyBtn = createButton("LISTO", GREEN);
        joinAsPlayerBtn = createButton("UNIRSE", PURPLE);
        joinAsPlayerBtn.setVisible(false);
        
        leaveBtn.addActionListener(e -> {
            if (listener != null) listener.onLeaveRoom();
        });
        
        readyBtn.addActionListener(e -> {
            if (!isSpectator && !isReady && listener != null) {
                isReady = true;
                readyBtn.setText("LISTO!");
                listener.onReady();
                updateStatus();
            }
        });
        
        joinAsPlayerBtn.addActionListener(e -> {
            if (isSpectator && listener != null) {
                listener.onJoinAsPlayer();
            }
        });
        
        panel.add(leaveBtn);
        panel.add(readyBtn);
        panel.add(joinAsPlayerBtn);
        
        return panel;
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            Color btnColor = color;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                Color c = isEnabled() ? (hover ? brighter(btnColor, 1.1f) : btnColor) : new Color(180, 180, 180);
                
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 3, h - 3, 14, 14));
                
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 3, 14, 14));
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            
            Color brighter(Color c, float f) { 
                return new Color(Math.min(255,(int)(c.getRed()*f)), Math.min(255,(int)(c.getGreen()*f)), Math.min(255,(int)(c.getBlue()*f))); 
            }
        };
        
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }
    
    public void updateRoom(GameRoom room) {
        if (room == null) return;
        
        SwingUtilities.invokeLater(() -> {
            roomNameLabel.setText(room.getRoomName());
            
            String countText = "Jugadores: " + room.getCurrentPlayers() + "/" + room.getMaxPlayers();
            if (room.getSpectatorCount() > 0) {
                countText += " | " + room.getSpectatorCount() + " observadores";
            }
            playersCountLabel.setText(countText);
            totalPlayers = room.getCurrentPlayers();
            maxPlayers = room.getMaxPlayers();
            
            // Mostrar botón "UNIRSE" si soy espectador y hay espacio
            if (isSpectator && !room.isGameStarted() && room.getCurrentPlayers() < room.getMaxPlayers()) {
                joinAsPlayerBtn.setVisible(true);
            } else {
                joinAsPlayerBtn.setVisible(false);
            }
            
            playerListPanel.removeAll();
            
            // Jugadores
            List<String> players = room.getPlayerNames();
            for (int i = 0; i < players.size(); i++) {
                String name = players.get(i);
                boolean isHost = (i == 0);
                boolean ready = readyPlayers.contains(name);
                playerListPanel.add(createPlayerEntry(name, isHost, ready, false));
                playerListPanel.add(Box.createVerticalStrut(8));
            }
            
            // Espectadores
            List<String> specs = room.getSpectatorNames();
            if (!specs.isEmpty()) {
                playerListPanel.add(Box.createVerticalStrut(10));
                JLabel specLabel = new JLabel("--- Observadores ---");
                specLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                specLabel.setForeground(new Color(148, 163, 184));
                specLabel.setAlignmentX(LEFT_ALIGNMENT);
                playerListPanel.add(specLabel);
                playerListPanel.add(Box.createVerticalStrut(8));
                
                for (String name : specs) {
                    playerListPanel.add(createPlayerEntry(name, false, false, true));
                    playerListPanel.add(Box.createVerticalStrut(6));
                }
            }
            
            playerListPanel.revalidate();
            playerListPanel.repaint();
            updateStatus();
        });
    }
    
    public void setPlayerReady(String playerName) {
        readyPlayers.add(playerName);
    }
    
    private void updateStatus() {
        int ready = readyPlayers.size() + (isReady ? 1 : 0);
        
        if (isSpectator) {
            statusLabel.setText("Modo espectador - Esperando inicio de partida");
            statusLabel.setForeground(new Color(139, 92, 246));
        } else if (totalPlayers < 2) {
            statusLabel.setText("Esperando mas jugadores... (minimo 2)");
            statusLabel.setForeground(ORANGE);
        } else if (ready < totalPlayers) {
            statusLabel.setText("Esperando que todos esten listos (" + ready + "/" + totalPlayers + ")");
            statusLabel.setForeground(ORANGE);
        } else {
            statusLabel.setText("Todos listos! Iniciando partida...");
            statusLabel.setForeground(GREEN);
        }
    }
    
    private JPanel createPlayerEntry(String name, boolean isHost, boolean ready, boolean isSpec) {
        JPanel entry = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (isSpec) {
                    bg = new Color(245, 243, 255);
                } else {
                    bg = ready ? new Color(220, 252, 231) : new Color(248, 250, 252);
                }
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                if (ready && !isSpec) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(GREEN);
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 9, 9));
                }
            }
        };
        entry.setOpaque(false);
        entry.setBorder(new EmptyBorder(12, 15, 12, 15));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        String prefix = "";
        if (isSpec) {
            prefix = "(obs) ";
        } else if (ready) {
            prefix = "[OK] ";
        } else if (isHost) {
            prefix = "[Host] ";
        }
        
        JLabel nameLabel = new JLabel(prefix + name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        if (isSpec) {
            nameLabel.setForeground(new Color(139, 92, 246));
        } else {
            nameLabel.setForeground(ready ? new Color(22, 163, 74) : new Color(51, 65, 85));
        }
        
        String roleText;
        if (isSpec) {
            roleText = "Observando";
        } else if (ready) {
            roleText = "Listo";
        } else if (isHost) {
            roleText = "Host";
        } else {
            roleText = "Esperando";
        }
        
        JLabel roleLabel = new JLabel(roleText);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        if (isSpec) {
            roleLabel.setForeground(new Color(139, 92, 246));
        } else {
            roleLabel.setForeground(ready ? GREEN : (isHost ? ORANGE : new Color(148, 163, 184)));
        }
        
        entry.add(nameLabel, BorderLayout.WEST);
        entry.add(roleLabel, BorderLayout.EAST);
        
        return entry;
    }
    
    public void setSpectator(boolean spectator) {
        this.isSpectator = spectator;
        this.isReady = false;
        this.readyPlayers.clear();
        
        if (spectator) {
            readyBtn.setText("OBSERVANDO");
            readyBtn.setEnabled(false);
        } else {
            readyBtn.setText("LISTO");
            readyBtn.setEnabled(true);
        }
        updateStatus();
    }
    
    public void reset() {
        isReady = false;
        readyPlayers.clear();
        if (isSpectator) {
            readyBtn.setText("OBSERVANDO");
            readyBtn.setEnabled(false);
        } else {
            readyBtn.setText("LISTO");
            readyBtn.setEnabled(true);
        }
        updateStatus();
    }
}
