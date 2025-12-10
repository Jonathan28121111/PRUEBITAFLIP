package flip7.comun;
import java.io.Serializable;
import java.util.*;

public class Jugador implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private int id;
    private List<Carta> cartasNumero = new ArrayList<>();
    private List<Carta> cartasModificador = new ArrayList<>();
    private List<Carta> cartasAccion = new ArrayList<>();
    private Carta cartaSegundaOportunidad;
    private int puntajeTotal;
    private int puntajeRonda;
    private boolean eliminado;
    private boolean plantado;
    private boolean congelado;
    private boolean conectado = true;
    
    public Jugador(String nombre, int id) { 
        this.nombre = nombre; 
        this.id = id; 
    }
    
    public void reiniciarParaNuevaRonda() { 
        cartasNumero.clear(); 
        cartasModificador.clear(); 
        cartasAccion.clear(); 
        cartaSegundaOportunidad = null; 
        puntajeRonda = 0; 
        eliminado = plantado = congelado = false; 
    }
    
    public void reiniciarParaNuevoJuego() { 
        reiniciarParaNuevaRonda(); 
        puntajeTotal = 0; 
    }
    
    public boolean agregarCarta(Carta c) {
        if (c == null) return false;
        switch (c.getTipo()) {
            case NUMERO: 
                if (tieneNumero(c.getValor())) return false; 
                cartasNumero.add(c); 
                return true;
            case MODIFICADOR: 
                cartasModificador.add(c); 
                return true;
            case SEGUNDA_OPORTUNIDAD: 
                if (cartaSegundaOportunidad == null) cartaSegundaOportunidad = c; 
                return true;
            case CONGELAR: 
                cartasAccion.add(c); 
                return true;
            case VOLTEAR_TRES: 
                cartasAccion.add(c); 
                return true;
            default: 
                return true;
        }
    }
    
    public void agregarCartaAccion(Carta c) { 
        if (c != null) cartasAccion.add(c); 
    }
    
    public boolean tieneNumero(int n) { 
        for (Carta c : cartasNumero) 
            if (c.getValor() == n) return true; 
        return false; 
    }
    
    public boolean tieneSegundaOportunidad() { 
        return cartaSegundaOportunidad != null; 
    }
    
    public Carta usarSegundaOportunidad() { 
        Carta c = cartaSegundaOportunidad; 
        cartaSegundaOportunidad = null; 
        return c; 
    }
    
    public void setCartaSegundaOportunidad(Carta c) { 
        cartaSegundaOportunidad = c; 
    }
    
    public int calcularPuntajeRonda() {
        if (eliminado) { puntajeRonda = 0; return 0; }
        int total = 0; 
        for (Carta c : cartasNumero) total += c.getValor();
        boolean tieneX2 = false; 
        int totalMod = 0;
        for (Carta c : cartasModificador) { 
            if (c.esX2()) tieneX2 = true; 
            else totalMod += c.getValor(); 
        }
        if (tieneX2) total *= 2;
        puntajeRonda = total + totalMod;
        if (cartasNumero.size() >= 7) puntajeRonda += 15;
        return puntajeRonda;
    }
    
    public boolean tieneVoltear7() { return cartasNumero.size() >= 7; }
    public boolean estaActivo() { return !eliminado && !plantado && !congelado && conectado; }
    public String getNombre() { return nombre; }
    public int getId() { return id; }
    public int getPuntajeTotal() { return puntajeTotal; }
    public void agregarAPuntajeTotal(int p) { puntajeTotal += p; }
    public int getPuntajeRonda() { return puntajeRonda; }
    public boolean estaEliminado() { return eliminado; }
    public void setEliminado(boolean b) { eliminado = b; }
    public boolean estaPlantado() { return plantado; }
    public void setPlantado(boolean s) { plantado = s; }
    public boolean estaCongelado() { return congelado; }
    public void setCongelado(boolean f) { congelado = f; if (f) plantado = true; }
    public boolean estaConectado() { return conectado; }
    public void setConectado(boolean c) { conectado = c; }
    public int getCantidadCartasNumero() { return cartasNumero.size(); }
    public List<Carta> getCartasNumero() { return cartasNumero; }
    public List<Carta> getCartasModificador() { return cartasModificador; }
    public List<Carta> getCartasAccion() { return cartasAccion; }
    
    public List<Carta> getTodasLasCartas() { 
        List<Carta> todas = new ArrayList<>(); 
        todas.addAll(cartasAccion); 
        if (cartaSegundaOportunidad != null) todas.add(cartaSegundaOportunidad); 
        todas.addAll(cartasModificador);
        todas.addAll(cartasNumero); 
        return todas; 
    }
}
