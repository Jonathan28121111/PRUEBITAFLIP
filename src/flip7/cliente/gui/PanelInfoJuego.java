package flip7.cliente.gui;

import flip7.comun.Jugador;
import flip7.comun.EstadoJuego;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PanelInfoJuego extends JPanel {
    private JLabel etiquetaRonda, etiquetaFase, etiquetaMazo;
    private JTextArea areaRegistro;
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    
    public PanelInfoJuego() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        JPanel cajaInfo = new JPanel() {
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
        cajaInfo.setLayout(new GridLayout(3, 1, 0, 8));
        cajaInfo.setBorder(new EmptyBorder(15, 18, 15, 18));
        cajaInfo.setOpaque(false);
        etiquetaRonda = crearEtiquetaInfo("RONDA", "1");
        etiquetaFase = crearEtiquetaInfo("FASE", "Esperando");
        etiquetaMazo = crearEtiquetaInfo("MAZO", "-- cartas");
        cajaInfo.add(etiquetaRonda);
        cajaInfo.add(etiquetaFase);
        cajaInfo.add(etiquetaMazo);
        JPanel cajaRegistro = new JPanel(new BorderLayout()) {
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
        cajaRegistro.setOpaque(false);
        cajaRegistro.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel tituloReg = new JLabel("REGISTRO");
        tituloReg.setFont(new Font("Arial", Font.BOLD, 11));
        tituloReg.setForeground(AZUL_OSCURO);
        tituloReg.setBorder(new EmptyBorder(0, 5, 8, 0));
        areaRegistro = new JTextArea();
        areaRegistro.setEditable(false);
        areaRegistro.setOpaque(false);
        areaRegistro.setForeground(new Color(51, 65, 85));
        areaRegistro.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaRegistro.setLineWrap(true);
        areaRegistro.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaRegistro);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(220, 140));
        cajaRegistro.add(tituloReg, BorderLayout.NORTH);
        cajaRegistro.add(scroll, BorderLayout.CENTER);
        add(cajaInfo, BorderLayout.NORTH);
        add(cajaRegistro, BorderLayout.CENTER);
    }
    
    private JLabel crearEtiquetaInfo(String titulo, String valor) {
        return new JLabel("<html><span style='color:#64748b;font-size:9px;font-weight:bold;'>" + titulo + "</span><br><span style='color:#1e293b;font-size:15px;font-weight:bold;'>" + valor + "</span></html>");
    }
    
    private void actualizarEtiqueta(JLabel etiqueta, String titulo, String valor, String color) {
        etiqueta.setText("<html><span style='color:#64748b;font-size:9px;font-weight:bold;'>" + titulo + "</span><br><span style='color:" + color + ";font-size:15px;font-weight:bold;'>" + valor + "</span></html>");
    }
    
    public void actualizarEstado(EstadoJuego e) {
        if (e == null) return;
        actualizarEtiqueta(etiquetaRonda, "RONDA", String.valueOf(e.getNumeroRonda()), "#1e293b");
        String fase = "?", colorFase = "#1e293b";
        switch (e.getFase()) {
            case ESPERANDO_JUGADORES: fase = "Esperando"; colorFase = "#f59e0b"; break;
            case REPARTIENDO: fase = "Repartiendo"; colorFase = "#3b82f6"; break;
            case JUGANDO: fase = "En juego"; colorFase = "#22c55e"; break;
            case FIN_RONDA: fase = "Fin ronda"; colorFase = "#f97316"; break;
            case FIN_JUEGO: fase = "Fin juego"; colorFase = "#8b5cf6"; break;
        }
        actualizarEtiqueta(etiquetaFase, "FASE", fase, colorFase);
        actualizarEtiqueta(etiquetaMazo, "MAZO", e.getTamanoMazo() + " cartas", "#64748b");
    }
    
    public void registrar(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaRegistro.append(msg + "\n");
            areaRegistro.setCaretPosition(areaRegistro.getDocument().getLength());
        });
    }
    
    public void mostrarPuntuaciones(java.util.List<Jugador> jugadores) {
        StringBuilder sb = new StringBuilder("\n=== PUNTUACIONES ===\n");
        jugadores.sort((a, b) -> b.getPuntajeTotal() - a.getPuntajeTotal());
        int pos = 1;
        for (Jugador j : jugadores) {
            String medalla = pos == 1 ? "1." : pos == 2 ? "2." : pos == 3 ? "3." : pos + ".";
            sb.append(medalla + " " + j.getNombre() + ": " + j.getPuntajeTotal() + "\n");
            pos++;
        }
        registrar(sb.toString());
    }
}
