package flip7.cliente.gui;

import flip7.comun.SalaJuego;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class PanelSalaEspera extends JPanel {
    private JLabel etiquetaNombreSala, etiquetaCantidadJugadores, etiquetaEstado;
    private JPanel panelListaJugadores;
    private JButton botonListo, botonSalir;
    private EscuchaSalaEspera escucha;
    private boolean estaListo = false;
    private boolean esEspectador = false;
    private Set<String> jugadoresListos = new HashSet<>();
    private int totalJugadores = 0;
    private JButton botonUnirseComoJugador;
    private int maxJugadores = 4;
    private boolean esHost = false;
    private JLabel etiquetaJugadoresListos;
    
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    private static final Color ROJO = new Color(248, 113, 113);
    private static final Color NARANJA = new Color(251, 146, 60);
    private static final Color MORADO = new Color(139, 92, 246);
    
    public interface EscuchaSalaEspera {
        void alListo();
        void alSalirSala();
        void alUnirseComoJugador();
    }
    
    public PanelSalaEspera(EscuchaSalaEspera escucha) {
        this.escucha = escucha;
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 60, 30, 60));
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearPanelCentro(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }
    
    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        etiquetaNombreSala = new JLabel("SALA DE ESPERA");
        etiquetaNombreSala.setFont(new Font("Arial", Font.BOLD, 28));
        etiquetaNombreSala.setForeground(AZUL_OSCURO);
        etiquetaCantidadJugadores = new JLabel("Jugadores: 0/0");
        etiquetaCantidadJugadores.setFont(new Font("Arial", Font.BOLD, 16));
        etiquetaCantidadJugadores.setForeground(new Color(100, 116, 139));
        cabecera.add(etiquetaNombreSala, BorderLayout.WEST);
        cabecera.add(etiquetaCantidadJugadores, BorderLayout.EAST);
        return cabecera;
    }
    
    private JPanel crearPanelCentro() {
        JPanel centro = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth(), getHeight(), 20, 20));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(AZUL_OSCURO);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 19, 19));
            }
        };
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel titulo = new JLabel("Jugadores en la sala:");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setForeground(new Color(51, 65, 85));
        panelListaJugadores = new JPanel();
        panelListaJugadores.setLayout(new BoxLayout(panelListaJugadores, BoxLayout.Y_AXIS));
        panelListaJugadores.setOpaque(false);
        JScrollPane scroll = new JScrollPane(panelListaJugadores);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        
        etiquetaEstado = new JLabel("Presiona LISTO para comenzar (minimo 2 jugadores)");
        etiquetaEstado.setFont(new Font("Arial", Font.ITALIC, 13));
        etiquetaEstado.setForeground(new Color(100, 116, 139));
        etiquetaEstado.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaEstado.setBorder(new EmptyBorder(15, 0, 5, 0));
        
        etiquetaJugadoresListos = new JLabel("");
        etiquetaJugadoresListos.setFont(new Font("Arial", Font.BOLD, 12));
        etiquetaJugadoresListos.setForeground(VERDE);
        etiquetaJugadoresListos.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaJugadoresListos.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        panelInferior.add(etiquetaEstado, BorderLayout.NORTH);
        panelInferior.add(etiquetaJugadoresListos, BorderLayout.CENTER);
        
        centro.add(titulo, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(panelInferior, BorderLayout.SOUTH);
        return centro;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        botonSalir = crearBoton("SALIR", ROJO);
        botonListo = crearBoton("LISTO", VERDE);
        botonUnirseComoJugador = crearBoton("UNIRSE", MORADO);
        botonUnirseComoJugador.setVisible(false);
        botonSalir.addActionListener(e -> { if (escucha != null) escucha.alSalirSala(); });
        botonListo.addActionListener(e -> {
            if (!esEspectador && !estaListo && escucha != null) {
                estaListo = true;
                botonListo.setText("LISTO!");
                escucha.alListo();
                actualizarEstado();
            }
        });
        botonUnirseComoJugador.addActionListener(e -> { if (esEspectador && escucha != null) escucha.alUnirseComoJugador(); });
        panel.add(botonSalir);
        panel.add(botonListo);
        panel.add(botonUnirseComoJugador);
        return panel;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto) {
            boolean hover = false;
            Color colorBtn = color;
            { addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { hover = true; repaint(); } public void mouseExited(MouseEvent e) { hover = false; repaint(); } }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color c = isEnabled() ? (hover ? masBrillante(colorBtn, 1.1f) : colorBtn) : new Color(180, 180, 180);
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
    
    public void actualizarSala(SalaJuego sala) {
        if (sala == null) return;
        SwingUtilities.invokeLater(() -> {
            etiquetaNombreSala.setText(sala.getNombreSala());
            String textoContador = "Jugadores: " + sala.getJugadoresActuales() + "/" + sala.getMaxJugadores();
            if (sala.getCantidadEspectadores() > 0) textoContador += " | " + sala.getCantidadEspectadores() + " observadores";
            etiquetaCantidadJugadores.setText(textoContador);
            totalJugadores = sala.getJugadoresActuales();
            maxJugadores = sala.getMaxJugadores();
            if (esEspectador && !sala.isJuegoIniciado() && sala.getJugadoresActuales() < sala.getMaxJugadores()) botonUnirseComoJugador.setVisible(true);
            else botonUnirseComoJugador.setVisible(false);
            panelListaJugadores.removeAll();
            List<String> jugadores = sala.getNombresJugadores();
            for (int i = 0; i < jugadores.size(); i++) {
                String nombre = jugadores.get(i);
                boolean esHostJ = (i == 0);
                boolean listo = jugadoresListos.contains(nombre);
                panelListaJugadores.add(crearEntradaJugador(nombre, esHostJ, listo, false));
                panelListaJugadores.add(Box.createVerticalStrut(8));
            }
            List<String> espectadores = sala.getNombresEspectadores();
            if (!espectadores.isEmpty()) {
                panelListaJugadores.add(Box.createVerticalStrut(10));
                JLabel etiquetaEsp = new JLabel("--- Observadores ---");
                etiquetaEsp.setFont(new Font("Arial", Font.ITALIC, 12));
                etiquetaEsp.setForeground(new Color(148, 163, 184));
                etiquetaEsp.setAlignmentX(LEFT_ALIGNMENT);
                panelListaJugadores.add(etiquetaEsp);
                panelListaJugadores.add(Box.createVerticalStrut(8));
                for (String nombre : espectadores) {
                    panelListaJugadores.add(crearEntradaJugador(nombre, false, false, true));
                    panelListaJugadores.add(Box.createVerticalStrut(6));
                }
            }
            panelListaJugadores.revalidate();
            panelListaJugadores.repaint();
            actualizarEstado();
        });
    }
    
    public void setJugadorListo(String nombreJugador) {
        jugadoresListos.add(nombreJugador);
    }
    
    public void actualizarJugadoresListos(List<String> listos) {
        if (listos != null) {
            jugadoresListos.clear();
            jugadoresListos.addAll(listos);
            actualizarEstado();
        }
    }
    
    private void actualizarEstado() {
        int listos = jugadoresListos.size() + (estaListo ? 1 : 0);
        if (esEspectador) {
            etiquetaEstado.setText("Modo espectador - Esperando inicio de partida");
            etiquetaEstado.setForeground(new Color(139, 92, 246));
            etiquetaJugadoresListos.setText("");
        } else if (totalJugadores < 2) {
            etiquetaEstado.setText("Esperando mas jugadores... (minimo 2)");
            etiquetaEstado.setForeground(NARANJA);
            etiquetaJugadoresListos.setText("");
        } else if (listos < totalJugadores) {
            etiquetaEstado.setText("Esperando que todos esten listos (" + listos + "/" + totalJugadores + ")");
            etiquetaEstado.setForeground(NARANJA);
            if (esHost && !jugadoresListos.isEmpty()) {
                StringBuilder sb = new StringBuilder("Listos: ");
                int count = 0;
                for (String nombre : jugadoresListos) {
                    if (count > 0) sb.append(", ");
                    sb.append(nombre);
                    count++;
                }
                etiquetaJugadoresListos.setText(sb.toString());
            } else {
                etiquetaJugadoresListos.setText("");
            }
        } else {
            etiquetaEstado.setText("Todos listos! Iniciando partida...");
            etiquetaEstado.setForeground(VERDE);
            etiquetaJugadoresListos.setText("");
        }
    }
    
    private JPanel crearEntradaJugador(String nombre, boolean esHostJ, boolean listo, boolean esEspec) {
        JPanel entrada = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondo;
                if (esEspec) fondo = new Color(245, 243, 255);
                else fondo = listo ? new Color(220, 252, 231) : new Color(248, 250, 252);
                g2.setColor(fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                if (listo && !esEspec) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(VERDE);
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 9, 9));
                }
            }
        };
        entrada.setOpaque(false);
        entrada.setBorder(new EmptyBorder(12, 15, 12, 15));
        entrada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        String prefijo = "";
        if (esEspec) prefijo = "(obs) ";
        else if (listo) prefijo = "[OK] ";
        else if (esHostJ) prefijo = "[Host] ";
        JLabel etiquetaNombre = new JLabel(prefijo + nombre);
        etiquetaNombre.setFont(new Font("Arial", Font.BOLD, 14));
        if (esEspec) etiquetaNombre.setForeground(new Color(139, 92, 246));
        else etiquetaNombre.setForeground(listo ? new Color(22, 163, 74) : new Color(51, 65, 85));
        String textoRol;
        if (esEspec) textoRol = "Observando";
        else if (listo) textoRol = "Listo";
        else if (esHostJ) textoRol = "Host";
        else textoRol = "Esperando";
        JLabel etiquetaRol = new JLabel(textoRol);
        etiquetaRol.setFont(new Font("Arial", Font.PLAIN, 12));
        if (esEspec) etiquetaRol.setForeground(new Color(139, 92, 246));
        else etiquetaRol.setForeground(listo ? VERDE : (esHostJ ? NARANJA : new Color(148, 163, 184)));
        entrada.add(etiquetaNombre, BorderLayout.WEST);
        entrada.add(etiquetaRol, BorderLayout.EAST);
        return entrada;
    }
    
    public void setEspectador(boolean espectador) {
        this.esEspectador = espectador;
        this.estaListo = false;
        this.jugadoresListos.clear();
        if (espectador) {
            botonListo.setText("OBSERVANDO");
            botonListo.setEnabled(false);
        } else {
            botonListo.setText("LISTO");
            botonListo.setEnabled(true);
        }
        actualizarEstado();
    }
    
    public void setEsHost(boolean esHost) {
        this.esHost = esHost;
    }
    
    public void reiniciar() {
        estaListo = false;
        jugadoresListos.clear();
        if (esEspectador) {
            botonListo.setText("OBSERVANDO");
            botonListo.setEnabled(false);
        } else {
            botonListo.setText("LISTO");
            botonListo.setEnabled(true);
        }
        actualizarEstado();
    }
}
