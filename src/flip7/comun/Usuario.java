package flip7.comun;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String nombreUsuario;
    private String contrasena;
    private int partidasJugadas;
    private int partidasGanadas;
    private int puntajeTotal;
    
    public Usuario(String nombreUsuario, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.partidasJugadas = 0;
        this.partidasGanadas = 0;
        this.puntajeTotal = 0;
    }
    
    public Usuario(int id, String nombreUsuario, int partidasJugadas, int partidasGanadas, int puntajeTotal) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.partidasJugadas = partidasJugadas;
        this.partidasGanadas = partidasGanadas;
        this.puntajeTotal = puntajeTotal;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public int getPartidasJugadas() { return partidasJugadas; }
    public void setPartidasJugadas(int g) { this.partidasJugadas = g; }
    public int getPartidasGanadas() { return partidasGanadas; }
    public void setPartidasGanadas(int w) { this.partidasGanadas = w; }
    public int getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(int s) { this.puntajeTotal = s; }
    
    public void agregarPartida(boolean gano, int puntaje) {
        partidasJugadas++;
        if (gano) partidasGanadas++;
        puntajeTotal += puntaje;
    }
}
