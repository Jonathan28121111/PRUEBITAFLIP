package flip7.cliente.gui;

import flip7.comun.Usuario;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class PanelRankings extends JPanel {
    private JTable tablaRankings;
    private DefaultTableModel modeloTabla;
    private JButton botonVolver, botonActualizar;
    private EscuchaRankings escucha;
    
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    private static final Color ORO = new Color(255, 215, 0);
    private static final Color PLATA = new Color(192, 192, 192);
    private static final Color BRONCE = new Color(205, 127, 50);
    
    public interface EscuchaRankings {
        void alVolver();
        void alActualizar();
    }
    
    public PanelRankings(EscuchaRankings escucha) {
        this.escucha = escucha;
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearTablaRankings(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }
    
    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("TABLA DE RANKINGS");
        titulo.setFont(new Font("Arial", Font.BOLD, 32));
        titulo.setForeground(AZUL_OSCURO);
        JLabel subtitulo = new JLabel("Los mejores jugadores de Voltear 7");
        subtitulo.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitulo.setForeground(new Color(100, 116, 139));
        JPanel panelTitulo = new JPanel(new BorderLayout(0, 5));
        panelTitulo.setOpaque(false);
        panelTitulo.add(titulo, BorderLayout.NORTH);
        panelTitulo.add(subtitulo, BorderLayout.CENTER);
        cabecera.add(panelTitulo, BorderLayout.WEST);
        return cabecera;
    }
    
    private JPanel crearTablaRankings() {
        JPanel panel = new JPanel(new BorderLayout()) {
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
        String[] columnas = {"Pos", "Jugador", "Partidas", "Ganadas", "% Victoria", "Puntos"};
        modeloTabla = new DefaultTableModel(columnas, 0) { public boolean isCellEditable(int row, int col) { return false; } };
        tablaRankings = new JTable(modeloTabla);
        tablaRankings.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaRankings.setRowHeight(45);
        tablaRankings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRankings.setShowGrid(false);
        tablaRankings.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaRankings.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaRankings.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablaRankings.getColumnModel().getColumn(3).setPreferredWidth(90);
        tablaRankings.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaRankings.getColumnModel().getColumn(5).setPreferredWidth(100);
        JTableHeader cabTabla = tablaRankings.getTableHeader();
        cabTabla.setFont(new Font("Arial", Font.BOLD, 14));
        cabTabla.setBackground(new Color(135, 206, 250));
        cabTabla.setForeground(new Color(30, 41, 59));
        cabTabla.setPreferredSize(new Dimension(0, 45));
        tablaRankings.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                if (isSelected) label.setBackground(new Color(219, 234, 254));
                else label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                if (row == 0) { label.setForeground(ORO); label.setText("1."); }
                else if (row == 1) { label.setForeground(PLATA); label.setText("2."); }
                else if (row == 2) { label.setForeground(BRONCE); label.setText("3."); }
                else { label.setForeground(new Color(51, 65, 85)); label.setText(String.valueOf(row + 1)); }
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (isSelected) { c.setBackground(new Color(219, 234, 254)); c.setForeground(AZUL_OSCURO); }
                else { c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)); c.setForeground(new Color(51, 65, 85)); }
                if (col == 1) { setFont(new Font("Arial", Font.BOLD, 14)); setHorizontalAlignment(SwingConstants.LEFT); }
                else { setFont(new Font("Arial", Font.PLAIN, 14)); setHorizontalAlignment(SwingConstants.CENTER); }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 1; i < tablaRankings.getColumnCount(); i++) tablaRankings.getColumnModel().getColumn(i).setCellRenderer(renderCentro);
        JScrollPane scroll = new JScrollPane(tablaRankings);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        botonActualizar = crearBoton("ACTUALIZAR", VERDE);
        botonVolver = crearBoton("VOLVER", AZUL_OSCURO);
        botonActualizar.addActionListener(e -> { if (escucha != null) escucha.alActualizar(); });
        botonVolver.addActionListener(e -> { if (escucha != null) escucha.alVolver(); });
        panel.add(botonVolver);
        panel.add(botonActualizar);
        return panel;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { hover = true; repaint(); } public void mouseExited(MouseEvent e) { hover = false; repaint(); } }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color c = hover ? masBrillante(color, 1.1f) : color;
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 3, h - 3, 14, 14));
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 3, 14, 14));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            Color masBrillante(Color c, float f) { return new Color(Math.min(255,(int)(c.getRed()*f)), Math.min(255,(int)(c.getGreen()*f)), Math.min(255,(int)(c.getBlue()*f))); }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    public void actualizarRankings(List<Usuario> usuarios) {
        SwingUtilities.invokeLater(() -> {
            modeloTabla.setRowCount(0);
            if (usuarios == null || usuarios.isEmpty()) return;
            usuarios.sort((a, b) -> Integer.compare(b.getPuntajeTotal(), a.getPuntajeTotal()));
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario usuario = usuarios.get(i);
                String porcentaje = "0%";
                if (usuario.getPartidasJugadas() > 0) {
                    int pct = (int) ((usuario.getPartidasGanadas() * 100.0) / usuario.getPartidasJugadas());
                    porcentaje = pct + "%";
                }
                modeloTabla.addRow(new Object[]{ i + 1, usuario.getNombreUsuario(), usuario.getPartidasJugadas(), usuario.getPartidasGanadas(), porcentaje, usuario.getPuntajeTotal() });
            }
        });
    }
}
