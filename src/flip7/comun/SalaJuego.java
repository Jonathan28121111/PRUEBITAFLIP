package flip7.comun;

import java.io.Serializable;
import java.util.*;

public class SalaJuego implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String idSala;
    private String nombreSala;
    private String nombreHost;
    private int idHost;
    private int maxJugadores;
    private List<String> nombresJugadores = new ArrayList<>();
    private List<String> nombresEspectadores = new ArrayList<>();
    private boolean juegoIniciado;
    private long tiempoCreacion;
    
    public SalaJuego(String idSala, String nombreSala, String nombreHost, int idHost, int maxJugadores) {
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.nombreHost = nombreHost;
        this.idHost = idHost;
        this.maxJugadores = maxJugadores;
        this.tiempoCreacion = System.currentTimeMillis();
        this.nombresJugadores.add(nombreHost);
    }
    
    public String getIdSala() { return idSala; }
    public String getNombreSala() { return nombreSala; }
    public String getNombreHost() { return nombreHost; }
    public int getIdHost() { return idHost; }
    public int getMaxJugadores() { return maxJugadores; }
    public int getJugadoresActuales() { return nombresJugadores.size(); }
    public int getCantidadEspectadores() { return nombresEspectadores.size(); }
    public List<String> getNombresJugadores() { return new ArrayList<>(nombresJugadores); }
    public List<String> getNombresEspectadores() { return new ArrayList<>(nombresEspectadores); }
    public boolean isJuegoIniciado() { return juegoIniciado; }
    public void setJuegoIniciado(boolean iniciado) { this.juegoIniciado = iniciado; }
    public long getTiempoCreacion() { return tiempoCreacion; }
    
    public boolean estaLlena() { return nombresJugadores.size() >= maxJugadores; }
    
    public void agregarJugador(String nombre) {
        if (!estaLlena() && !nombresJugadores.contains(nombre)) {
            nombresJugadores.add(nombre);
        }
    }
    
    public void agregarEspectador(String nombre) {
        if (!nombresEspectadores.contains(nombre)) {
            nombresEspectadores.add(nombre);
        }
    }
    
    public void removerJugador(String nombre) {
        nombresJugadores.remove(nombre);
    }
    
    public void removerEspectador(String nombre) {
        nombresEspectadores.remove(nombre);
    }
    
    public boolean esEspectador(String nombre) {
        return nombresEspectadores.contains(nombre);
    }
    
    public String toString() {
        return nombreSala + " (" + getJugadoresActuales() + "/" + maxJugadores + ")";
    }
}
