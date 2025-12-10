package flip7.cliente.gui;

import flip7.comun.Jugador;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class PanelJugador extends JPanel {
    private Jugador jugador;
    private boolean esMiTurno, esJugadorLocal;
    private PanelCartas panelCartas;
    
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    private static final Color NARANJA = new Color(251, 146, 60);
    private static final Color ROJO = new Color(248, 113, 113);
    private static final Color CYAN = new Color(103, 232, 249);
    
    public PanelJugador(boolean esJugadorLocal) {
        this.esJugadorLocal = esJugadorLocal;
        setLayout(new BorderLayout());
        setOpaque(false);
        panelCartas = new PanelCartas();
        add(panelCartas, BorderLayout.CENTER);
    }
    
    public void setJugador(Jugador j) {
        jugador = j;
        if (jugador != null) panelCartas.setCartas(jugador.getTodasLasCartas());
        else panelCartas.limpiar();
        repaint();
    }
    
    public void setTurnoActual(boolean t) { esMiTurno = t; repaint(); }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(100, 150, 200, 40));
        g2.fill(new RoundRectangle2D.Float(4, 4, w - 4, h - 4, 16, 16));
        Color colorFondo = esJugadorLocal ? new Color(240, 253, 244) : new Color(255, 255, 255);
        g2.setColor(colorFondo);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 4, 16, 16));
        Color colorBorde = esJugadorLocal ? VERDE : new Color(203, 213, 225);
        if (esMiTurno && jugador != null && jugador.estaActivo()) {
            colorBorde = AZUL_OSCURO;
            g2.setColor(new Color(66, 133, 244, 40));
            g2.setStroke(new BasicStroke(8f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 4, h - 4, 16, 16));
        }
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(colorBorde);
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 6, h - 6, 15, 15));
        if (jugador != null) {
            dibujarCabecera(g2, w);
            dibujarEstado(g2, w, h);
        } else {
            g2.setFont(new Font("Arial", Font.ITALIC, 13));
            g2.setColor(new Color(148, 163, 184));
            String texto = "Esperando...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, h / 2);
        }
        super.paintComponent(g);
    }
    
    private void dibujarCabecera(Graphics2D g2, int w) {
        g2.setColor(esJugadorLocal ? new Color(220, 252, 231) : new Color(241, 245, 249));
        g2.fill(new RoundRectangle2D.Float(3, 3, w - 10, 38, 13, 13));
        g2.fillRect(3, 25, w - 10, 16);
        Color colorIndicador;
        if (jugador.estaEliminado()) colorIndicador = ROJO;
        else if (jugador.estaCongelado()) colorIndicador = CYAN;
        else if (jugador.estaPlantado()) colorIndicador = VERDE;
        else if (esMiTurno && jugador.estaActivo()) colorIndicador = AZUL_OSCURO;
        else colorIndicador = new Color(148, 163, 184);
        g2.setColor(colorIndicador);
        g2.fillOval(12, 12, 14, 14);
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillOval(14, 14, 6, 6);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(30, 41, 59));
        String nombre = jugador.getNombre() + (esJugadorLocal ? " (Tu)" : "");
        g2.drawString(nombre, 32, 24);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String puntaje = jugador.getPuntajeTotal() + "";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(AZUL_OSCURO);
        g2.drawString(puntaje, w - fm.stringWidth(puntaje) - 15, 26);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(100, 116, 139));
        g2.drawString("pts", w - 12 - fm.stringWidth(puntaje) + fm.stringWidth(puntaje) + 3, 26);
    }
    
    private void dibujarEstado(Graphics2D g2, int w, int h) {
        String estado = null;
        Color colorEstado = null, colorFondo = null;
        if (jugador.estaEliminado()) { estado = "ELIMINADO"; colorEstado = ROJO; colorFondo = new Color(254, 226, 226); }
        else if (jugador.estaCongelado()) { estado = "CONGELADO"; colorEstado = new Color(6, 182, 212); colorFondo = new Color(207, 250, 254); }
        else if (jugador.estaPlantado()) { estado = "PLANTADO"; colorEstado = new Color(22, 163, 74); colorFondo = new Color(220, 252, 231); }
        else if (esMiTurno && jugador.estaActivo()) { estado = esJugadorLocal ? "TU TURNO" : "JUGANDO"; colorEstado = AZUL_OSCURO; colorFondo = new Color(219, 234, 254); }
        if (estado != null) {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int anchoEst = fm.stringWidth(estado) + 20;
            int xEst = (w - anchoEst) / 2;
            g2.setColor(colorFondo);
            g2.fill(new RoundRectangle2D.Float(xEst, h - 30, anchoEst, 22, 11, 11));
            g2.setColor(colorEstado);
            g2.drawString(estado, xEst + 10, h - 14);
        }
    }
    
    public Insets getInsets() { return new Insets(45, 10, 35, 10); }
}
