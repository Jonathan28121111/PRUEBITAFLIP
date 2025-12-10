package flip7.cliente.gui;

import flip7.comun.Carta;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class PanelCartas extends JPanel {
    private java.util.List<Carta> cartas = new ArrayList<>();
    
    public PanelCartas() { setOpaque(false); }
    
    public void setCartas(java.util.List<Carta> cartas) { this.cartas = new ArrayList<>(cartas); repaint(); }
    public void limpiar() { cartas.clear(); repaint(); }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int anchoC = 58, altoC = 85, espaciado = 6, y = 5;
        if (cartas.isEmpty()) {
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fill(new RoundRectangle2D.Float(5, y, anchoC, altoC, 10, 10));
            return;
        }
        int anchoTotal = cartas.size() * anchoC + (cartas.size() - 1) * espaciado;
        int anchoDisp = getWidth() - 10;
        if (anchoTotal > anchoDisp && cartas.size() > 1) {
            espaciado = (anchoDisp - anchoC) / cartas.size() - anchoC + espaciado;
            if (espaciado < -anchoC + 15) espaciado = -anchoC + 15;
        }
        int x = 5;
        for (Carta carta : cartas) { dibujarCarta(g2, carta, x, y, anchoC, altoC); x += anchoC + espaciado; }
    }
    
    private void dibujarCarta(Graphics2D g2, Carta carta, int x, int y, int w, int h) {
        Color colorFondo, colorBorde, colorTexto;
        switch (carta.getTipo()) {
            case NUMERO: colorFondo = new Color(255, 252, 240); colorBorde = new Color(180, 165, 100); colorTexto = getColorNum(carta.getValor()); break;
            case MODIFICADOR: colorFondo = new Color(255, 220, 100); colorBorde = new Color(200, 160, 50); colorTexto = new Color(150, 100, 30); break;
            case CONGELAR: colorFondo = new Color(180, 220, 245); colorBorde = new Color(100, 160, 200); colorTexto = new Color(40, 100, 150); break;
            case VOLTEAR_TRES: colorFondo = new Color(255, 240, 120); colorBorde = new Color(200, 180, 60); colorTexto = new Color(50, 120, 100); break;
            case SEGUNDA_OPORTUNIDAD: colorFondo = new Color(230, 100, 120); colorBorde = new Color(180, 60, 80); colorTexto = new Color(255, 250, 250); break;
            default: colorFondo = Color.WHITE; colorBorde = Color.GRAY; colorTexto = Color.BLACK;
        }
        g2.setColor(new Color(0, 0, 0, 60)); g2.fill(new RoundRectangle2D.Float(x + 3, y + 3, w, h, 10, 10));
        g2.setColor(colorFondo); g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
        g2.setStroke(new BasicStroke(2.5f)); g2.setColor(colorBorde); g2.draw(new RoundRectangle2D.Float(x + 4, y + 4, w - 8, h - 8, 6, 6));
        String texto = getTexto(carta);
        Font fuente = carta.getTipo() == Carta.TipoCarta.NUMERO ? new Font("Georgia", Font.BOLD, 28) : carta.getTipo() == Carta.TipoCarta.MODIFICADOR ? new Font("Georgia", Font.BOLD, 22) : new Font("Arial", Font.BOLD, 11);
        g2.setFont(fuente); FontMetrics fm = g2.getFontMetrics();
        g2.setColor(colorTexto); g2.drawString(texto, x + (w - fm.stringWidth(texto)) / 2, y + h/2 + fm.getAscent()/3);
        String sub = getSub(carta);
        if (sub != null) { g2.setFont(new Font("Arial", Font.BOLD, 7)); fm = g2.getFontMetrics(); g2.setColor(new Color(colorTexto.getRed(), colorTexto.getGreen(), colorTexto.getBlue(), 140)); g2.drawString(sub, x + (w - fm.stringWidth(sub)) / 2, y + h - 10); }
    }
    
    private Color getColorNum(int v) {
        switch (v) { case 0: return new Color(80, 80, 80); case 1: case 2: return new Color(110, 90, 60); case 3: case 6: return new Color(20, 130, 110); case 4: case 5: return new Color(170, 60, 90); case 7: case 8: return new Color(220, 140, 70); case 9: case 10: return new Color(120, 70, 140); default: return new Color(70, 110, 160); }
    }
    private String getTexto(Carta c) { switch (c.getTipo()) { case NUMERO: return String.valueOf(c.getValor()); case MODIFICADOR: return c.esX2() ? "x2" : "+" + c.getValor(); case CONGELAR: return "CONG"; case VOLTEAR_TRES: return "VL 3"; case SEGUNDA_OPORTUNIDAD: return "2da"; default: return "?"; } }
    private String getSub(Carta c) { if (c.getTipo() == Carta.TipoCarta.NUMERO) { String[] n = {"CERO", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE"}; return c.getValor() < n.length ? n[c.getValor()] : null; } if (c.getTipo() == Carta.TipoCarta.SEGUNDA_OPORTUNIDAD) return "OPORTUNIDAD"; if (c.getTipo() == Carta.TipoCarta.CONGELAR) return "CONGELAR"; if (c.getTipo() == Carta.TipoCarta.VOLTEAR_TRES) return "TRES"; return null; }
}
