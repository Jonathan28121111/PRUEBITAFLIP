package flip7.client.gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class ConnectionDialog extends JDialog {
    private JTextField hostField, portField, nameField;
    private String host; private int port; private String playerName; private boolean confirmed;
    
    public ConnectionDialog(JFrame parent) {
        super(parent, "Conectar", true);
        setUndecorated(true); setSize(360, 300); setLocationRelativeTo(parent);
        JPanel main = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(32,40,55), 0, getHeight(), new Color(22,28,40));
                g2.setPaint(gp); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-4, 18, 18));
                g2.setStroke(new BasicStroke(2)); g2.setColor(new Color(55,70,95));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-6, getHeight()-6, 17, 17));
            }
        };
        main.setOpaque(false); main.setBorder(new EmptyBorder(20, 25, 20, 25));
        JPanel titlePanel = new JPanel(new BorderLayout()); titlePanel.setOpaque(false);
        JLabel logo = new JLabel("FLIP 7"); logo.setFont(new Font("Segoe UI", Font.BOLD, 36)); logo.setForeground(new Color(255,200,60)); logo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel sub = new JLabel("Juego de Cartas"); sub.setFont(new Font("Segoe UI", Font.PLAIN, 11)); sub.setForeground(new Color(130,140,160)); sub.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(logo, BorderLayout.CENTER); titlePanel.add(sub, BorderLayout.SOUTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false); fields.setBorder(new EmptyBorder(15, 0, 15, 0));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 0, 5, 0);
        hostField = mkField("localhost"); portField = mkField("5555"); nameField = mkField("Jugador");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; fields.add(mkLbl("Servidor"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; fields.add(hostField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; fields.add(mkLbl("Puerto"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; fields.add(portField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; fields.add(mkLbl("Tu nombre"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; fields.add(nameField, gbc);
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0)); btns.setOpaque(false);
        JButton cancelBtn = mkBtn("CANCELAR", new Color(180,70,70));
        JButton connectBtn = mkBtn("CONECTAR", new Color(70,160,90));
        cancelBtn.addActionListener(e -> { confirmed = false; dispose(); });
        connectBtn.addActionListener(e -> connect());
        nameField.addActionListener(e -> connect());
        btns.add(cancelBtn); btns.add(connectBtn);
        main.add(titlePanel, BorderLayout.NORTH); main.add(fields, BorderLayout.CENTER); main.add(btns, BorderLayout.SOUTH);
        setContentPane(main);
        final Point[] drag = {null};
        main.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); } });
        main.addMouseMotionListener(new MouseMotionAdapter() { public void mouseDragged(MouseEvent e) { Point p = getLocation(); setLocation(p.x + e.getX() - drag[0].x, p.y + e.getY() - drag[0].y); } });
    }
    private JLabel mkLbl(String t) { JLabel l = new JLabel(t); l.setForeground(new Color(140,150,170)); l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); l.setBorder(new EmptyBorder(0,0,0,12)); return l; }
    private JTextField mkField(String t) { JTextField f = new JTextField(t); f.setBackground(new Color(45,55,70)); f.setForeground(Color.WHITE); f.setCaretColor(new Color(100,170,255)); f.setFont(new Font("Segoe UI", Font.PLAIN, 13)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,75,95)), new EmptyBorder(8,10,8,10))); f.setPreferredSize(new Dimension(180,36)); return f; }
    private JButton mkBtn(String t, Color c) {
        JButton b = new JButton(t) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { hover = true; repaint(); } public void mouseExited(MouseEvent e) { hover = false; repaint(); } }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? brighter(c, 1.15f) : c;
                g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            private Color brighter(Color col, float f) { return new Color(Math.min(255,(int)(col.getRed()*f)), Math.min(255,(int)(col.getGreen()*f)), Math.min(255,(int)(col.getBlue()*f))); }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setPreferredSize(new Dimension(130,40));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
    private void connect() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { nameField.setBackground(new Color(80,50,50)); return; }
        try { port = Integer.parseInt(portField.getText().trim()); } catch (Exception e) { portField.setBackground(new Color(80,50,50)); return; }
        host = hostField.getText().trim(); if (host.isEmpty()) host = "localhost";
        playerName = name; confirmed = true; dispose();
    }
    public boolean showDialog() { setVisible(true); return confirmed; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getPlayerName() { return playerName; }
}
