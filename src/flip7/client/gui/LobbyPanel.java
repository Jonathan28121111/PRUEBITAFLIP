package flip7.client.gui;

import flip7.common.GameRoom;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class LobbyPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JButton createBtn, joinBtn, refreshBtn;
    private JTextField nameField;
    private LobbyListener listener;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color BLUE_LIGHT = new Color(135, 206, 250);
    private static final Color GREEN = new Color(74, 222, 128);
    
    public interface LobbyListener {
        void onCreateRoom(String roomName, String playerName, int maxPlayers);
        void onJoinRoom(String roomId, String playerName, boolean asSpectator);
        void onRefresh();
    }
    
    public LobbyPanel(LobbyListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createRoomListPanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("SALAS DISPONIBLES") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(BLUE_DARK);
                g2.drawString(getText(), 0, getHeight() - 5);
            }
        };
        title.setFont(new Font("Arial", Font.BOLD, 28));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        namePanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel("Tu nombre:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(51, 65, 85));
        
       nameField = new JTextField(15);
nameField.setFont(new Font("Arial", Font.PLAIN, 14));
nameField.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
    new EmptyBorder(8, 12, 8, 12)
));
nameField.setText("Jugador");

// ✅ BLOQUEAR EDICIÓN
nameField.setEditable(false);        // no se puede escribir
nameField.setFocusable(false);       // no se puede seleccionar
nameField.setBackground(Color.WHITE);
        
        namePanel.add(nameLabel);
        namePanel.add(nameField);
        
        header.add(title, BorderLayout.WEST);
        header.add(namePanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        String[] columns = {"Nombre de Sala", "Host", "Jugadores", "Estado", "ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("Arial", Font.PLAIN, 14));
        roomTable.setRowHeight(40);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setShowGrid(false);
        roomTable.setIntercellSpacing(new Dimension(0, 5));
        
        roomTable.getColumnModel().getColumn(4).setMinWidth(0);
        roomTable.getColumnModel().getColumn(4).setMaxWidth(0);
        
        JTableHeader tableHeader = roomTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setBackground(BLUE_LIGHT);
        tableHeader.setForeground(new Color(30, 41, 59));
        tableHeader.setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                    c.setForeground(BLUE_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(51, 65, 85));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < roomTable.getColumnCount(); i++) {
            roomTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    joinSelectedRoom(false);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(roomTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        
        createBtn = createButton("CREAR SALA", GREEN);
        joinBtn = createButton("UNIRSE", BLUE_DARK);
        JButton spectateBtn = createButton("OBSERVAR", new Color(139, 92, 246));
        refreshBtn = createButton("<<", new Color(100, 116, 139));
        refreshBtn.setPreferredSize(new Dimension(60, 50));
        
        createBtn.addActionListener(e -> showCreateRoomDialog());
        joinBtn.addActionListener(e -> joinSelectedRoom(false));
        spectateBtn.addActionListener(e -> joinSelectedRoom(true));
        refreshBtn.addActionListener(e -> { if (listener != null) listener.onRefresh(); });
        
        panel.add(refreshBtn);
        panel.add(createBtn);
        panel.add(joinBtn);
        panel.add(spectateBtn);
        
        return panel;
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
                Color c = color;
                if (hover) c = brighter(c, 1.1f);
                
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 3, h - 3, 14, 14));
                
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 3, 14, 14));
                
                g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,80), 0, h/2, new Color(255,255,255,0)));
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 7, h/2 - 2, 12, 12));
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
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
    
    private void showCreateRoomDialog() {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu nombre primero", "Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField roomNameField = new JTextField("Sala de " + playerName);
        JSpinner maxPlayersSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 6, 1));
        
        panel.add(new JLabel("Nombre de la sala:"));
        panel.add(roomNameField);
        panel.add(new JLabel("Maximo de jugadores:"));
        panel.add(maxPlayersSpinner);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Nueva Sala", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String roomName = roomNameField.getText().trim();
            int maxPlayers = (Integer) maxPlayersSpinner.getValue();
            
            if (roomName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la sala no puede estar vacio", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (listener != null) {
                listener.onCreateRoom(roomName, playerName, maxPlayers);
            }
        }
    }
    
    private void joinSelectedRoom(boolean asSpectator) {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una sala para unirte", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu nombre primero", "Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        String roomId = (String) tableModel.getValueAt(row, 4);
        if (listener != null) {
            listener.onJoinRoom(roomId, playerName, asSpectator);
        }
    }
    
    public void updateRoomList(List<GameRoom> rooms) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            
            if (rooms == null || rooms.isEmpty()) {
                return;
            }
            
            for (GameRoom room : rooms) {
                String status;
                if (room.isGameStarted()) {
                    status = "En juego";
                } else if (room.isFull()) {
                    status = "Llena";
                } else {
                    status = "Esperando";
                }
                
                String players = room.getCurrentPlayers() + "/" + room.getMaxPlayers();
                if (room.getSpectatorCount() > 0) {
                    players += " +" + room.getSpectatorCount() + " obs";
                }
                
                tableModel.addRow(new Object[]{
                    room.getRoomName(),
                    room.getHostName(),
                    players,
                    status,
                    room.getRoomId()
                });
            }
        });
    }
    
    public String getPlayerName() {
        return nameField.getText().trim();
    }
    
    public void setPlayerName(String name) {
        nameField.setText(name);
    }
}
