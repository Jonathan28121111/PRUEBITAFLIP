package flip7.common;

import java.io.Serializable;

public class EstadisticasUsuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String nombreUsuario;
    private int partidasJugadas;
    private int partidasGanadas;
    private int puntosTotales;
    
    public EstadisticasUsuario(int id, String nombreUsuario, int partidasJugadas, 
                              int partidasGanadas, int puntosTotales) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.partidasJugadas = partidasJugadas;
        this.partidasGanadas = partidasGanadas;
        this.puntosTotales = puntosTotales;
    }
    
    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public int getPartidasJugadas() { return partidasJugadas; }
    public int getPartidasGanadas() { return partidasGanadas; }
    public int getPartidasPerdidas() { return partidasJugadas - partidasGanadas; }
    public int getPuntosTotales() { return puntosTotales; }
    
    public double getPorcentajeVictoria() {
        if (partidasJugadas == 0) return 0;
        return (partidasGanadas * 100.0) / partidasJugadas;
    }
}