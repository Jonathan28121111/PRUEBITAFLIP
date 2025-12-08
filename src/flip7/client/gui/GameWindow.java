package flip7.client.gui;

import flip7.client.GameClient;
import flip7.common.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class GameWindow extends JFrame implements GameClient.GameClientListener {
    private GameClient client = new GameClient();
    private int myPlayerId = -1;
    private boolean isMyTurn, gameStarted, isSpectator;
    private Map<Integer, PlayerPanel> playerPanels = new HashMap<>();
    
    // Paneles principales
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private WaitingRoomPanel waitingRoomPanel;
    private JPanel gamePanel;
    
    // Componentes del juego
    private JPanel playersPanel;
    private GameInfoPanel infoPanel;
    private JButton hitBtn, standBtn, readyBtn;
    private JLabel turnIndicator;
    private JTextArea chatArea;
    private JTextField chatInput;
    
    private static final Color BLUE_LIGHT = new Color(135, 206, 250);
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color ORANGE = new Color(251, 146, 60);
    private static final Color RED = new Color(248, 113, 113);
    
    public GameWindow() {
        super("Flip 7 - Juego de Cartas");
        client.addListener(this);
        initUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 850);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (client.isConnected()) client.disconnect();
            }
        });
    }
    
    private void initUI() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint bg = new GradientPaint(0, 0, BLUE_LIGHT, 0, getHeight(), new Color(248, 250, 252));
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Nubes decorativas
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g2.setColor(Color.WHITE);
                g2.fillOval(-50, -80, 300, 200);
                g2.fillOval(150, -50, 250, 180);
                g2.fillOval(getWidth() - 250, -60, 350, 220);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        };
        
        // 1. Pantalla de Login
        loginPanel = new LoginPanel(new LoginPanel.LoginListener() {
            public void onLogin(String username, String password, String host, int port) {
                new Thread(() -> {
                    if (!client.isConnected()) {
                        if (!client.connect(host, port, username)) {
                            loginPanel.onConnectionFailed();
                            return;
                        }
                    }
                    client.login(username, password);
                }).start();
            }
            public void onRegister(String username, String password, String host, int port) {
                new Thread(() -> {
                    if (!client.isConnected()) {
                        if (!client.connect(host, port, username)) {
                            loginPanel.onConnectionFailed();
                            return;
                        }
                    }
                    client.register(username, password);
                }).start();
            }
        });
        
        // 2. Pantalla de Lobby
        lobbyPanel = new LobbyPanel(new LobbyPanel.LobbyListener() {
            public void onCreateRoom(String roomName, String playerName, int maxPlayers) {
                client.createRoom(roomName, maxPlayers);
            }
            public void onJoinRoom(String roomId, String playerName, boolean asSpectator) {
                isSpectator = asSpectator;
                if (asSpectator) {
                    client.joinRoomAsSpectator(roomId);
                } else {
                    client.joinRoom(roomId);
                }
            }
            public void onRefresh() {
                client.requestRooms();
            }
        });
        
        // 3. Sala de espera
        waitingRoomPanel = new WaitingRoomPanel(new WaitingRoomPanel.WaitingRoomListener() {
            public void onReady() {
                client.ready();
            }
            public void onLeaveRoom() {
                client.leaveRoom();
                showPanel("lobby");
            }
            public void onJoinAsPlayer() {
                // Espectador quiere unirse como jugador
                String roomId = client.getCurrentRoomId();
                if (roomId != null) {
                    client.leaveRoom();
                    isSpectator = false;
                    client.joinRoom(roomId);
                }
            }
        });
        
        // 4. Pantalla de juego
        gamePanel = createGamePanel();
        
        mainContainer.add(loginPanel, "login");
        mainContainer.add(lobbyPanel, "lobby");
        mainContainer.add(waitingRoomPanel, "waiting");
        mainContainer.add(gamePanel, "game");
        
        setContentPane(mainContainer);
        showPanel("login");
    }
    
    private void showPanel(String name) {
        cardLayout.show(mainContainer, name);
    }
    
    private JPanel createGamePanel() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setOpaque(false);
        
        JPanel header = createGameHeader();
        
        JPanel centerArea = new JPanel(new BorderLayout(15, 0));
        centerArea.setOpaque(false);
        centerArea.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JPanel tablePanel = createTablePanel();
        JPanel rightPanel = createRightPanel();
        
        centerArea.add(tablePanel, BorderLayout.CENTER);
        centerArea.add(rightPanel, BorderLayout.EAST);
        
        JPanel controls = createControls();
        
        main.add(header, BorderLayout.NORTH);
        main.add(centerArea, BorderLayout.CENTER);
        main.add(controls, BorderLayout.SOUTH);
        
        return main;
    }
    
    private JPanel createTablePanel() {
        JPanel table = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(4, 4, w - 4, h - 4, 28, 28));
                
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 5, h - 5, 28, 28));
                
                g2.setStroke(new BasicStroke(4f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(2, 2, w - 9, h - 9, 26, 26));
                
                // Logo central
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.setColor(new Color(100, 180, 246));
                g2.fillOval(w/2 - 80, h/2 - 80, 160, 160);
                
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.setFont(new Font("Arial", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(BLUE_DARK);
                g2.drawString("FLIP", w/2 - fm.stringWidth("FLIP")/2, h/2 - 8);
                g2.setFont(new Font("Arial", Font.BOLD, 56));
                fm = g2.getFontMetrics();
                g2.drawString("7", w/2 - fm.stringWidth("7")/2, h/2 + 45);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        };
        table.setOpaque(false);
        table.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        playersPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        playersPanel.setOpaque(false);
        
        table.add(playersPanel, BorderLayout.CENTER);
        return table;
    }
    
    private JPanel createGameHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(100, 180, 246));
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        JLabel logo = new JLabel("FLIP 7");
        logo.setFont(new Font("Arial", Font.BOLD, 32));
        logo.setForeground(BLUE_DARK);
        
        turnIndicator = new JLabel("");
        turnIndicator.setFont(new Font("Arial", Font.BOLD, 16));
        turnIndicator.setForeground(BLUE_DARK);
        
        header.add(logo, BorderLayout.WEST);
        header.add(turnIndicator, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(250, 0));
        
        infoPanel = new GameInfoPanel();
        
        JPanel chatPanel = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 16, 16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 16, 16));
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-5, getHeight()-5, 15, 15));
            }
        };
        chatPanel.setOpaque(false);
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel chatTitle = new JLabel("💬 CHAT");
        chatTitle.setFont(new Font("Arial", Font.BOLD, 12));
        chatTitle.setForeground(BLUE_DARK);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setOpaque(false);
        chatArea.setForeground(new Color(51, 65, 85));
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        chatArea.setLineWrap(true);
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setOpaque(false);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.setBorder(null);
        
        chatInput = new JTextField();
        chatInput.setBackground(new Color(248, 250, 252));
        chatInput.setForeground(new Color(30, 41, 59));
        chatInput.setCaretColor(BLUE_DARK);
        chatInput.setFont(new Font("Arial", Font.PLAIN, 12));
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            new EmptyBorder(10, 12, 10, 12)
        ));
        chatInput.addActionListener(e -> {
            String t = chatInput.getText().trim();
            if (!t.isEmpty() && client.isConnected()) {
                client.sendChat(t);
                chatInput.setText("");
            }
        });
        
        chatPanel.add(chatTitle, BorderLayout.NORTH);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        
        right.add(infoPanel, BorderLayout.NORTH);
        right.add(chatPanel, BorderLayout.CENTER);
        
        return right;
    }
    
   private JPanel createControls() {
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(100, 180, 246));
            g2.fillRect(0, 0, getWidth(), 4);
        }
    };
    controls.setBorder(new EmptyBorder(12, 0, 18, 0));
    
    readyBtn = createButton("LISTO", GREEN);
    hitBtn = createButton("🂠 PEDIR CARTA", BLUE_DARK);
    standBtn = createButton("✋ PLANTARSE", ORANGE);
    JButton backBtn = createButton("SALIR", RED);
    
    readyBtn.addActionListener(e -> {
        client.ready();
        readyBtn.setEnabled(false);
        readyBtn.setText("ESPERANDO...");
    });
    hitBtn.addActionListener(e -> { if (isMyTurn) client.hit(); });
    standBtn.addActionListener(e -> { if (isMyTurn) client.stand(); });
    
    // BOTÓN SALIR MODIFICADO
    backBtn.addActionListener(e -> {
        client.leaveRoom();
        gameStarted = false;
        cleanupGameState(); // ⭐ AGREGAR ESTA LÍNEA
        showPanel("lobby");
    });
    
    readyBtn.setEnabled(false);
    hitBtn.setEnabled(false);
    standBtn.setEnabled(false);
    
    controls.add(backBtn);
    controls.add(readyBtn);
    controls.add(hitBtn);
    controls.add(standBtn);
    
    return controls;
}
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text) {
            boolean hover = false;
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
                Color c = isEnabled() ? (hover ? brighter(color, 1.1f) : color) : new Color(203, 213, 225);
                
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 3, h - 3, 14, 14));
                
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 3, 14, 14));
                
                g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,80), 0, h/2, new Color(255,255,255,0)));
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 7, h/2 - 2, 12, 12));
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(isEnabled() ? Color.WHITE : new Color(148, 163, 184));
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            
            Color brighter(Color c, float f) { return new Color(Math.min(255,(int)(c.getRed()*f)), Math.min(255,(int)(c.getGreen()*f)), Math.min(255,(int)(c.getBlue()*f))); }
        };
        
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }
    
    private void updateControls() {
        SwingUtilities.invokeLater(() -> {
            hitBtn.setEnabled(gameStarted && isMyTurn && !isSpectator);
            standBtn.setEnabled(gameStarted && isMyTurn && !isSpectator);
        });
    }
    
private void updatePlayerPanels(GameState s) {
    if (s == null) return;
    SwingUtilities.invokeLater(() -> {
        java.util.List<Player> players = s.getPlayers();
        
        // Filtrar solo jugadores conectados
        java.util.List<Player> connectedPlayers = new java.util.ArrayList<>();
        for (Player p : players) {
            if (p.isConnected()) {
                connectedPlayers.add(p);
            }
        }
        
        // Si cambió la cantidad de jugadores, reconstruir paneles
        if (playerPanels.size() != connectedPlayers.size()) {
            playersPanel.removeAll();
            playerPanels.clear();
            
            for (Player p : connectedPlayers) {
                PlayerPanel pp = new PlayerPanel(p.getId() == myPlayerId);
                pp.setPlayer(p);
                playerPanels.put(p.getId(), pp);
                playersPanel.add(pp);
            }
            
            // Llenar espacios vacíos
            for (int i = connectedPlayers.size(); i < 6; i++) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                playersPanel.add(empty);
            }
            playersPanel.revalidate();
        }
        
        // Actualizar paneles existentes
        Player curr = s.getCurrentPlayer();
        for (Player p : connectedPlayers) {
            PlayerPanel pp = playerPanels.get(p.getId());
            if (pp != null) {
                pp.setPlayer(p);
                pp.setCurrentTurn(curr != null && curr.getId() == p.getId());
            }
        }
        
        infoPanel.updateGameState(s);
        playersPanel.repaint();
    });
}

    // === LISTENERS ===
    public void onConnected(int id, String name) {
        // Solo conexión TCP establecida, esperar login
    }
    
public void onLoginSuccess(User user) {
    SwingUtilities.invokeLater(() -> {
        myPlayerId = user.getId();
        client.setPlayerName(user.getUsername());
        lobbyPanel.setPlayerName(user.getUsername());
        showPanel("lobby");
        infoPanel.log("Conexión establecida");
        infoPanel.log("  Partidas: " + user.getGamesPlayed() + " | Ganadas: " + user.getGamesWon());
    });
}
    
    public void onLoginFailed(String reason) {
        loginPanel.onLoginFailed(reason);
    }
    
public void onRegisterSuccess(User user) {
    SwingUtilities.invokeLater(() -> {
        loginPanel.resetButtons();
        
        JOptionPane.showMessageDialog(this, 
            "Usuario registrado correctamente!", 
            "Registro Exitoso", 
            JOptionPane.INFORMATION_MESSAGE);
        
        myPlayerId = user.getId();
        client.setPlayerName(user.getUsername());
        lobbyPanel.setPlayerName(user.getUsername());
        showPanel("lobby");
        infoPanel.log("Registro exitoso!");
    });
}//holajajaja
private void cleanupGameState() {
    SwingUtilities.invokeLater(() -> {
        playersPanel.removeAll();
        playerPanels.clear();
        
        // Llenar con paneles vacíos
        for (int i = 0; i < 6; i++) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            playersPanel.add(empty);
        }
        
        playersPanel.revalidate();
        playersPanel.repaint();
        
        chatArea.setText("");
        turnIndicator.setText("");
    });
} 
    public void onRegisterFailed(String reason) {
        loginPanel.onRegisterFailed(reason);
    }
    
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            gameStarted = false;
            isMyTurn = false;
            showPanel("login");
            JOptionPane.showMessageDialog(this, "Desconectado del servidor", "Aviso", JOptionPane.WARNING_MESSAGE);
        });
    }
    
    public void onRoomList(java.util.List<GameRoom> rooms) {
        lobbyPanel.updateRoomList(rooms);
    }
    
    public void onRoomCreated(GameRoom room, int playerId) {
        SwingUtilities.invokeLater(() -> {
            myPlayerId = playerId;
            waitingRoomPanel.setSpectator(false);
            waitingRoomPanel.updateRoom(room);
            showPanel("waiting");
        });
    }
    
    public void onRoomJoined(GameRoom room, int playerId) {
        SwingUtilities.invokeLater(() -> {
            myPlayerId = playerId;
            // Si playerId es -1, es espectador. Si es >= 0, es jugador
            isSpectator = (playerId < 0);
            waitingRoomPanel.setSpectator(isSpectator);
            waitingRoomPanel.updateRoom(room);
            showPanel("waiting");
        });
    }
    
    public void onRoomUpdate(GameRoom room) {
        waitingRoomPanel.updateRoom(room);
    }
    
    public void onRoomError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void onPlayerJoined(int id, String name) { infoPanel.log("+ " + name + " se unió"); }
public void onPlayerLeft(int id, String name) { 
    SwingUtilities.invokeLater(() -> {
        infoPanel.log("- " + name + " salió");
        
      
        PlayerPanel panel = playerPanels.remove(id);
        if (panel != null) {
            playersPanel.remove(panel);
            
            // Agregar panel vacío para mantener el grid
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            playersPanel.add(empty);
            
            playersPanel.revalidate();
            playersPanel.repaint();
        }
    });
}
    
    public void onGameStart(java.util.List<Player> players) {
        SwingUtilities.invokeLater(() -> {
            gameStarted = true;
            readyBtn.setEnabled(false);
            readyBtn.setText("EN JUEGO");
            showPanel("game");
            infoPanel.log("\n*** JUEGO INICIADO ***\n");
        });
    }
    
    public void onRoundStart(int round) { infoPanel.log("\n─── RONDA " + round + " ───"); }
    
    public void onYourTurn(int id) {
        SwingUtilities.invokeLater(() -> {
            isMyTurn = (id == myPlayerId);
            updateControls();
            if (isMyTurn && !isSpectator) {
                turnIndicator.setText("TU TURNO!");
                Toolkit.getDefaultToolkit().beep();
            } else {
                GameState s = client.getCurrentGameState();
                if (s != null) {
                    Player p = s.getPlayerById(id);
                    if (p != null) turnIndicator.setText("Turno: " + p.getName());
                }
            }
        });
    }
    
    public void onCardDealt(int id, Card c) {
        GameState s = client.getCurrentGameState();
        if (s != null) { Player p = s.getPlayerById(id); if (p != null) infoPanel.log(p.getName() + " ← " + c); }
    }
    
    public void onPlayerBusted(int id, Card c) {
        GameState s = client.getCurrentGameState();
        if (s != null) { Player p = s.getPlayerById(id); if (p != null) infoPanel.log("X " + p.getName() + " ELIMINADO con " + c); }
        if (id == myPlayerId) { isMyTurn = false; updateControls(); }
    }
    
    public void onPlayerStand(int id) {
        GameState s = client.getCurrentGameState();
        if (s != null) { Player p = s.getPlayerById(id); if (p != null) infoPanel.log(p.getName() + " se planto"); }
        if (id == myPlayerId) { isMyTurn = false; updateControls(); }
    }
    
    public void onPlayerFrozen(int id) {
        GameState s = client.getCurrentGameState();
        if (s != null) { Player p = s.getPlayerById(id); if (p != null) infoPanel.log(p.getName() + " CONGELADO"); }
    }
    
    public void onActionCardDrawn(int id, Card c) {
        GameState s = client.getCurrentGameState();
        if (s != null) { Player p = s.getPlayerById(id); if (p != null) infoPanel.log("* " + p.getName() + " -> " + c); }
    }
    
    public void onChooseActionTarget(Card card, java.util.List<Player> active) {
        if (isSpectator) return;
        SwingUtilities.invokeLater(() -> {
            String[] opts = new String[active.size()];
            for (int i = 0; i < active.size(); i++) opts[i] = active.get(i).getName();
            String sel = (String) JOptionPane.showInputDialog(this, "¿A quién asignas " + card + "?", "Carta de Acción", JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
            if (sel != null) for (Player p : active) if (p.getName().equals(sel)) { client.assignActionCard(p.getId(), card); break; }
        });
    }
    
    public void onRoundEnd(java.util.List<Player> players, int round) {
        SwingUtilities.invokeLater(() -> {
            isMyTurn = false; turnIndicator.setText(""); updateControls();
            infoPanel.log("\n─── FIN RONDA " + round + " ───");
            for (Player p : players) infoPanel.log("  " + p.getName() + ": +" + p.getRoundScore() + " → " + p.getTotalScore());
            infoPanel.showScoreboard(players);
        });
    }
    
   public void onGameEnd(java.util.List<Player> players, int winnerId) {
    SwingUtilities.invokeLater(() -> {
        gameStarted = false; 
        isMyTurn = false; 
        updateControls();
        
        Player w = null; 
        for (Player p : players) {
            if (p.getId() == winnerId) { 
                w = p; 
                break; 
            }
        }
        
        String msg = w != null ? w.getName() + " GANA con " + w.getTotalScore() + " pts!" : "Fin";
        infoPanel.log("\n*** " + msg + " ***\n");
        
        if (!isSpectator) {
            int option = JOptionPane.showOptionDialog(this, 
                msg + "\n\n¿Quieres jugar otra partida?", 
                "Fin del Juego!", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"REVANCHA", "SALIR"},
                "REVANCHA");
            
            if (option == 0) {
                // Revancha
                waitingRoomPanel.reset();
                cleanupGameState(); // ⭐ AGREGAR
                showPanel("waiting");
            } else {
                // Salir
                client.leaveRoom();
                cleanupGameState(); // ⭐ AGREGAR
                showPanel("lobby");
            }
        } else {
            int option = JOptionPane.showOptionDialog(this, 
                msg + "\n\n¿Que quieres hacer?", 
                "Fin del Juego!", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"SEGUIR OBSERVANDO", "SALIR"},
                "SEGUIR OBSERVANDO");
            
            if (option == 0) {
                waitingRoomPanel.reset();
                cleanupGameState(); // ⭐ AGREGAR
                showPanel("waiting");
            } else {
                client.leaveRoom();
                cleanupGameState(); // ⭐ AGREGAR
                showPanel("lobby");
            }
        }
    });
}
    
    public void onGameStateUpdate(GameState s) { updatePlayerPanels(s); }
    
    public void onChatMessage(int id, String name, String msg) {
        SwingUtilities.invokeLater(() -> { chatArea.append(name + ": " + msg + "\n"); chatArea.setCaretPosition(chatArea.getDocument().getLength()); });
    }
    
    public void onError(String msg) {
        SwingUtilities.invokeLater(() -> { infoPanel.log("! " + msg); JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); });
    }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> { GameWindow w = new GameWindow(); w.setVisible(true); });
    }
}
