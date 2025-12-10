package flip7.cliente.gui;

import flip7.comun.SalaJuego;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class PanelLobby extends JPanel {
    private JTable tablaSalas;
    private DefaultTableModel modeloTabla;
    private JButton botonCrear, botonUnirse, botonActualizar;
    private JTextField campoNombre;
    private EscuchaLobby escucha;
    
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    
    public interface EscuchaLobby {
        void alCrearSala(String nombreSala, String nombreJugador, int maxJugadores);
        void alUnirseSala(String idSala, String nombreJugador, boolean comoEspectador);
        void alActualizar();
        void alSalir();
        void alVerRankings();
    }
    
    public PanelLobby(EscuchaLobby escucha) {
        this.escucha = escucha;
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearPanelListaSalas(), BorderLayout.CENTER);
        add(crearPanelAcciones(), BorderLayout.SOUTH);
    }
    
    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("SALAS DISPONIBLES");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(AZUL_OSCURO);
        JPanel panelNombre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelNombre.setOpaque(false);
        JLabel etiquetaNombre = new JLabel("Tu nombre:");
        etiquetaNombre.setFont(new Font("Arial", Font.PLAIN, 14));
        etiquetaNombre.setForeground(new Color(51, 65, 85));
        campoNombre = new JTextField(15);
        campoNombre.setFont(new Font("Arial", Font.PLAIN, 14));
        campoNombre.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2), new EmptyBorder(8, 12, 8, 12)));
        campoNombre.setText("Jugador");
        campoNombre.setEditable(false);
        campoNombre.setFocusable(false);
        campoNombre.setBackground(Color.WHITE);
        panelNombre.add(etiquetaNombre);
        panelNombre.add(campoNombre);
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(panelNombre, BorderLayout.EAST);
        return cabecera;
    }
    
    private JPanel crearPanelListaSalas() {
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
        String[] columnas = {"Nombre de Sala", "Host", "Jugadores", "Estado", "ID"};
        modeloTabla = new DefaultTableModel(columnas, 0) { public boolean isCellEditable(int row, int col) { return false; } };
        tablaSalas = new JTable(modeloTabla);
        tablaSalas.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaSalas.setRowHeight(40);
        tablaSalas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSalas.setShowGrid(false);
        tablaSalas.getColumnModel().getColumn(4).setMinWidth(0);
        tablaSalas.getColumnModel().getColumn(4).setMaxWidth(0);
        JTableHeader cabTabla = tablaSalas.getTableHeader();
        cabTabla.setFont(new Font("Arial", Font.BOLD, 14));
        cabTabla.setBackground(new Color(135, 206, 250));
        cabTabla.setForeground(new Color(30, 41, 59));
        cabTabla.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer renderCelda = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (isSelected) { c.setBackground(new Color(219, 234, 254)); c.setForeground(AZUL_OSCURO); }
                else { c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)); c.setForeground(new Color(51, 65, 85)); }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        renderCelda.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaSalas.getColumnCount(); i++) tablaSalas.getColumnModel().getColumn(i).setCellRenderer(renderCelda);
        tablaSalas.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) unirseSalaSeleccionada(false); } });
        JScrollPane scroll = new JScrollPane(tablaSalas);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        botonActualizar = crearBoton("<<", new Color(100, 116, 139));
        botonCrear = crearBoton("CREAR SALA", VERDE);
        botonUnirse = crearBoton("UNIRSE", AZUL_OSCURO);
        JButton botonObservar = crearBoton("OBSERVAR", new Color(139, 92, 246));
        JButton botonRankings = crearBoton("VER RANKINGS", new Color(139, 92, 246));
        botonActualizar.addActionListener(e -> { if (escucha != null) escucha.alSalir(); });
        botonCrear.addActionListener(e -> mostrarDialogoCrearSala());
        botonUnirse.addActionListener(e -> unirseSalaSeleccionada(false));
        botonObservar.addActionListener(e -> unirseSalaSeleccionada(true));
        botonRankings.addActionListener(e -> { if (escucha != null) escucha.alVerRankings(); });
        panel.add(botonActualizar);
        panel.add(botonCrear);
        panel.add(botonUnirse);
        panel.add(botonObservar);
        panel.add(botonRankings);
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
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void mostrarDialogoCrearSala() {
        String nombreJugador = campoNombre.getText().trim();
        if (nombreJugador.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa tu nombre primero", "Error", JOptionPane.WARNING_MESSAGE); campoNombre.requestFocus(); return; }
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField campoNombreSala = new JTextField("Sala de " + nombreJugador);
        JSpinner spinnerMaxJugadores = new JSpinner(new SpinnerNumberModel(4, 2, 6, 1));
        panel.add(new JLabel("Nombre de la sala:"));
        panel.add(campoNombreSala);
        panel.add(new JLabel("Maximo de jugadores:"));
        panel.add(spinnerMaxJugadores);
        int resultado = JOptionPane.showConfirmDialog(this, panel, "Crear Nueva Sala", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado == JOptionPane.OK_OPTION) {
            String nombreSala = campoNombreSala.getText().trim();
            int maxJugadores = (Integer) spinnerMaxJugadores.getValue();
            if (nombreSala.isEmpty()) { JOptionPane.showMessageDialog(this, "El nombre de la sala no puede estar vacio", "Error", JOptionPane.WARNING_MESSAGE); return; }
            if (escucha != null) escucha.alCrearSala(nombreSala, nombreJugador, maxJugadores);
        }
    }
    
    private void unirseSalaSeleccionada(boolean comoEspectador) {
        int fila = tablaSalas.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona una sala para unirte", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
        String nombreJugador = campoNombre.getText().trim();
        if (nombreJugador.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa tu nombre primero", "Error", JOptionPane.WARNING_MESSAGE); campoNombre.requestFocus(); return; }
        String idSala = (String) modeloTabla.getValueAt(fila, 4);
        if (escucha != null) escucha.alUnirseSala(idSala, nombreJugador, comoEspectador);
    }
    
    public void actualizarListaSalas(List<SalaJuego> salas) {
        SwingUtilities.invokeLater(() -> {
            modeloTabla.setRowCount(0);
            if (salas == null || salas.isEmpty()) return;
            for (SalaJuego sala : salas) {
                String estado;
                if (sala.isJuegoIniciado()) estado = "En juego";
                else if (sala.estaLlena()) estado = "Llena";
                else estado = "Esperando";
                String jugadores = sala.getJugadoresActuales() + "/" + sala.getMaxJugadores();
                if (sala.getCantidadEspectadores() > 0) jugadores += " +" + sala.getCantidadEspectadores() + " obs";
                modeloTabla.addRow(new Object[]{ sala.getNombreSala(), sala.getNombreHost(), jugadores, estado, sala.getIdSala() });
            }
        });
    }
    
    public String getNombreJugador() { return campoNombre.getText().trim(); }
    public void setNombreJugador(String nombre) { campoNombre.setText(nombre); }
}
