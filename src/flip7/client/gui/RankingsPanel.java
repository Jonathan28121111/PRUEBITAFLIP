package flip7.client.gui;

import flip7.common.User;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class RankingsPanel extends JPanel {
    private JTable rankingsTable;
    private DefaultTableModel tableModel;
    private JButton backBtn, refreshBtn;
    private RankingsListener listener;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color BLUE_LIGHT = new Color(135, 206, 250);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color SILVER = new Color(192, 192, 192);
    private static final Color BRONZE = new Color(205, 127, 50);
    
    public interface RankingsListener {
        void onBack();
        void onRefresh();
    }
    
    public RankingsPanel(RankingsListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createRankingsTable(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("🏆 TABLA DE RANKINGS") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(BLUE_DARK);
                g2.drawString(getText(), 0, getHeight() - 5);
            }
        };
        title.setFont(new Font("Arial", Font.BOLD, 32));
        
        JLabel subtitle = new JLabel("Los mejores jugadores de Flip 7");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(new Color(100, 116, 139));
        
        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createRankingsTable() {
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
        
        String[] columns = {"Pos", "Jugador", "Partidas", "Ganadas", "% Victoria", "Puntos"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        rankingsTable = new JTable(tableModel);
        rankingsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        rankingsTable.setRowHeight(45);
        rankingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankingsTable.setShowGrid(false);
        rankingsTable.setIntercellSpacing(new Dimension(0, 5));
        
        // Configurar anchos de columnas
        rankingsTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Pos
        rankingsTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Jugador
        rankingsTable.getColumnModel().getColumn(2).setPreferredWidth(90);  // Partidas
        rankingsTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Ganadas
        rankingsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // % Victoria
        rankingsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Puntos
        
        JTableHeader tableHeader = rankingsTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setBackground(BLUE_LIGHT);
        tableHeader.setForeground(new Color(30, 41, 59));
        tableHeader.setPreferredSize(new Dimension(0, 45));
        
        // Renderer personalizado para la columna de posición
        rankingsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                
                if (isSelected) {
                    label.setBackground(new Color(219, 234, 254));
                } else {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                
                // Colores especiales para top 3
                if (row == 0) {
                    label.setForeground(GOLD);
                    label.setText("🥇 1");
                } else if (row == 1) {
                    label.setForeground(SILVER);
                    label.setText("🥈 2");
                } else if (row == 2) {
                    label.setForeground(BRONZE);
                    label.setText("🥉 3");
                } else {
                    label.setForeground(new Color(51, 65, 85));
                    label.setText(String.valueOf(row + 1));
                }
                
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
        
        // Renderer para otras columnas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
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
                
                // Hacer el nombre del jugador en negrita
                if (column == 1) {
                    setFont(new Font("Arial", Font.BOLD, 14));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setFont(new Font("Arial", Font.PLAIN, 14));
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        
        for (int i = 1; i < rankingsTable.getColumnCount(); i++) {
            rankingsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scroll = new JScrollPane(rankingsTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        
        refreshBtn = createButton("↻ ACTUALIZAR", GREEN);
        backBtn = createButton("← VOLVER", BLUE_DARK);
        
        refreshBtn.addActionListener(e -> { 
            if (listener != null) listener.onRefresh(); 
        });
        
        backBtn.addActionListener(e -> { 
            if (listener != null) listener.onBack(); 
        });
        
        panel.add(backBtn);
        panel.add(refreshBtn);
        
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
                Color c = hover ? brighter(color, 1.1f) : color;
                
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
    
    public void updateRankings(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            
            if (users == null || users.isEmpty()) {
                return;
            }
            
            // Ordenar por puntos totales (descendente)
            users.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
            
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                
                String winRate = "0%";
                if (user.getGamesPlayed() > 0) {
                    int percentage = (int) ((user.getGamesWon() * 100.0) / user.getGamesPlayed());
                    winRate = percentage + "%";
                }
                
                tableModel.addRow(new Object[]{
                    i + 1,                        // Posición
                    user.getUsername(),           // Jugador
                    user.getGamesPlayed(),        // Partidas
                    user.getGamesWon(),           // Ganadas
                    winRate,                      // % Victoria
                    user.getTotalScore()          // Puntos
                });
            }
        });
    }
}