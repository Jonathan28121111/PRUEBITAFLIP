package flip7.comun;
import java.io.Serializable;
import java.awt.Color;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum TipoCarta { NUMERO, MODIFICADOR, CONGELAR, VOLTEAR_TRES, SEGUNDA_OPORTUNIDAD }
    private TipoCarta tipo;
    private int valor;
    private boolean esX2;
    
    public Carta(TipoCarta tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.esX2 = (tipo == TipoCarta.MODIFICADOR && valor == -1);
    }
    
    public TipoCarta getTipo() { return tipo; }
    public int getValor() { return valor; }
    public boolean esX2() { return esX2; }
    public boolean esCartaAccion() { return tipo == TipoCarta.CONGELAR || tipo == TipoCarta.VOLTEAR_TRES || tipo == TipoCarta.SEGUNDA_OPORTUNIDAD; }
    public boolean esCartaNumero() { return tipo == TipoCarta.NUMERO; }
    public boolean esCartaModificador() { return tipo == TipoCarta.MODIFICADOR; }
    
    public String toString() {
        switch (tipo) {
            case NUMERO: return "Numero " + valor;
            case MODIFICADOR: return esX2 ? "X2" : "+" + valor;
            case CONGELAR: return "CONGELAR";
            case VOLTEAR_TRES: return "VOLTEAR 3";
            case SEGUNDA_OPORTUNIDAD: return "2a OPORTUNIDAD";
            default: return "?";
        }
    }
    
    public String getNombreMostrar() {
        switch (tipo) {
            case NUMERO: return String.valueOf(valor);
            case MODIFICADOR: return esX2 ? "X2" : "+" + valor;
            case CONGELAR: return "FRZ";
            case VOLTEAR_TRES: return "VL3";
            case SEGUNDA_OPORTUNIDAD: return "2ND";
            default: return "?";
        }
    }
    
    public Color getColorCarta() {
        switch (tipo) {
            case NUMERO: return new Color(55, 120, 180);
            case MODIFICADOR: return esX2 ? new Color(200, 160, 40) : new Color(45, 160, 90);
            case CONGELAR: return new Color(60, 150, 200);
            case VOLTEAR_TRES: return new Color(200, 120, 50);
            case SEGUNDA_OPORTUNIDAD: return new Color(130, 90, 180);
            default: return Color.GRAY;
        }
    }
}
