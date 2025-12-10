package flip7.juego;

import flip7.comun.Jugador;
import flip7.comun.Carta;
import flip7.comun.Mazo;
import flip7.comun.EstadoJuego;
import java.util.*;

public class LogicaJuego {
    private EstadoJuego estadoJuego = new EstadoJuego();
    private Mazo mazo = new Mazo();
    private List<EscuchaEventosJuego> escuchas = new ArrayList<>();
    private boolean esperandoObjetivoAccion = false;
    private int jugadorEsperandoAccion = -1;
    private Carta cartaAccionPendiente = null;
    
    private boolean procesandoVoltear3 = false;
    private int idJugadorVoltear3 = -1;
    private int cartasRestantesVoltear3 = 0;
    
    public interface EscuchaEventosJuego {
        void alRepartirCarta(int idJugador, Carta carta);
        void alEliminarJugador(int idJugador, Carta carta);
        void alPlantarseJugador(int idJugador);
        void alCongelarJugador(int idJugador);
        void alRobarCartaAccion(int idJugador, Carta carta);
        void alFinRonda(List<Jugador> jugadores, int numeroRonda);
        void alFinJuego(Jugador ganador);
        void alCambiarTurno(int idJugador);
        void alActualizarEstado(EstadoJuego estado);
        void alNecesitarObjetivoAccion(int idJugador, Carta carta, List<Jugador> jugadoresActivos);
    }
    
    public void agregarEscucha(EscuchaEventosJuego e) { escuchas.add(e); }
    
    public int agregarJugador(String nombre) {
        int id = estadoJuego.getJugadores().size();
        estadoJuego.getJugadores().add(new Jugador(nombre, id));
        return id;
    }
    
    public void removerJugador(int id) {
        Jugador j = estadoJuego.getJugadorPorId(id);
        if (j != null) j.setConectado(false);
    }
    
    public boolean puedeIniciarJuego() { return estadoJuego.getJugadores().size() >= 2; }
    
    public void iniciarJuego() {
        if (!puedeIniciarJuego()) return;
        estadoJuego.setFase(EstadoJuego.Fase.REPARTIENDO);
        estadoJuego.setNumeroRonda(1);
        mazo.reiniciar();
        iniciarRonda();
    }
    
    public void iniciarRonda() {
        for (Jugador j : estadoJuego.getJugadores()) j.reiniciarParaNuevaRonda();
        estadoJuego.setFase(EstadoJuego.Fase.REPARTIENDO);
        
        esperandoObjetivoAccion = false;
        jugadorEsperandoAccion = -1;
        cartaAccionPendiente = null;
        procesandoVoltear3 = false;
        idJugadorVoltear3 = -1;
        cartasRestantesVoltear3 = 0;
        
        repartirCartasIniciales();
        
        estadoJuego.setFase(EstadoJuego.Fase.JUGANDO);
        
        int indiceInicio = estadoJuego.getIndiceRepartidor();
        Jugador jugadorInicio = estadoJuego.getJugadores().get(indiceInicio);
        if (!jugadorInicio.estaConectado()) {
            indiceInicio = getSiguienteIndiceJugadorActivo(indiceInicio);
            if (indiceInicio == -1) indiceInicio = 0;
        }
        
        estadoJuego.setIndiceJugadorActual(indiceInicio);
        notificarCambioTurno();
        notificarActualizacionEstado();
    }
    
    private void repartirCartasIniciales() {
        List<Jugador> jugadores = estadoJuego.getJugadores();
        int inicio = (estadoJuego.getIndiceRepartidor() + 1) % jugadores.size();
        
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get((inicio + i) % jugadores.size());
            if (!jugador.estaConectado()) continue;
            
            Carta carta = mazo.robarCarta();
            if (carta != null) {
                if (carta.esCartaAccion()) {
                    mazo.descartar(carta);
                    i--;
                } else {
                    boolean exito = jugador.agregarCarta(carta);
                    if (exito) {
                        notificarCartaRepartida(jugador.getId(), carta);
                    }
                }
            }
        }
        actualizarInfoMazo();
    }
    
    public void jugadorPide(int idJugador) {
        if (esperandoObjetivoAccion) return;
        
        Jugador jugador = estadoJuego.getJugadorPorId(idJugador);
        Jugador jugadorActual = estadoJuego.getJugadorActual();
        
        if (jugador == null || jugadorActual == null || jugadorActual.getId() != idJugador) return;
        if (!jugador.estaActivo()) return;
        
        Carta carta = mazo.robarCarta();
        if (carta == null) return;
        
        procesarCartaParaJugador(jugador, carta, false);
        
        if (jugador.tieneVoltear7() || estadoJuego.esFinRonda()) {
            terminarRonda();
            return;
        }
        
        if (!esperandoObjetivoAccion && !procesandoVoltear3) {
            avanzarTurno();
            notificarActualizacionEstado();
        }
    }
    
    private void procesarCartaParaJugador(Jugador jugador, Carta carta, boolean esDeVoltear3) {
        if (carta.esCartaAccion()) {
            notificarCartaRepartida(jugador.getId(), carta);
            notificarCartaAccionRobada(jugador.getId(), carta);
            
            if (carta.getTipo() == Carta.TipoCarta.SEGUNDA_OPORTUNIDAD) {
                if (!jugador.tieneSegundaOportunidad()) {
                    jugador.setCartaSegundaOportunidad(carta);
                    notificarCartaRepartida(jugador.getId(), carta);
                    actualizarInfoMazo();
                    notificarActualizacionEstado();
                    return;
                }
            }
            
            List<Jugador> objetivosValidos = getObjetivosValidosParaAccion(jugador, carta);
            
            if (objetivosValidos.isEmpty()) {
                mazo.descartar(carta);
                actualizarInfoMazo();
                return;
            }
            
            if (objetivosValidos.size() == 1) {
                aplicarCartaAccion(objetivosValidos.get(0).getId(), carta, jugador.getId());
                
                if (procesandoVoltear3 && cartasRestantesVoltear3 > 0) {
                    continuarVoltearTres(jugador);
                }
            } else {
                esperandoObjetivoAccion = true;
                jugadorEsperandoAccion = jugador.getId();
                cartaAccionPendiente = carta;
                notificarNecesitaObjetivoAccion(jugador.getId(), carta, objetivosValidos);
            }
        } else {
            boolean exito = jugador.agregarCarta(carta);
            if (!exito && carta.esCartaNumero()) {
                manejarEliminacion(jugador, carta);
            } else {
                notificarCartaRepartida(jugador.getId(), carta);
            }
        }
        actualizarInfoMazo();
    }
    
    private List<Jugador> getObjetivosValidosParaAccion(Jugador desdeJugador, Carta carta) {
        List<Jugador> validos = new ArrayList<>();
        
        for (Jugador j : estadoJuego.getJugadores()) {
            if (!j.estaConectado()) continue;
            if (!j.estaActivo() && carta.getTipo() != Carta.TipoCarta.SEGUNDA_OPORTUNIDAD) continue;
            
            if (carta.getTipo() == Carta.TipoCarta.SEGUNDA_OPORTUNIDAD) {
                if (!j.tieneSegundaOportunidad()) {
                    if (desdeJugador.tieneSegundaOportunidad() && j.getId() == desdeJugador.getId()) {
                        continue;
                    }
                    validos.add(j);
                }
            } else {
                if (j.estaActivo()) {
                    validos.add(j);
                }
            }
        }
        
        return validos;
    }
    
    public void asignarCartaAccion(int desdeId, int objetivoId, Carta carta) {
        if (!esperandoObjetivoAccion || jugadorEsperandoAccion != desdeId) return;
        
        Jugador objetivo = estadoJuego.getJugadorPorId(objetivoId);
        Jugador desdeJugador = estadoJuego.getJugadorPorId(desdeId);
        if (objetivo == null || desdeJugador == null) return;
        
        if (carta.getTipo() == Carta.TipoCarta.SEGUNDA_OPORTUNIDAD) {
            if (objetivo.tieneSegundaOportunidad()) return;
            if (desdeJugador.tieneSegundaOportunidad() && objetivoId == desdeId) return;
        }
        
        esperandoObjetivoAccion = false;
        jugadorEsperandoAccion = -1;
        cartaAccionPendiente = null;
        
        aplicarCartaAccion(objetivoId, carta, desdeId);
        
        if (procesandoVoltear3 && cartasRestantesVoltear3 > 0) {
            Jugador jugadorVoltear3 = estadoJuego.getJugadorPorId(idJugadorVoltear3);
            if (jugadorVoltear3 != null && !jugadorVoltear3.estaEliminado()) {
                continuarVoltearTres(jugadorVoltear3);
                return;
            }
        }
        
        if (!procesandoVoltear3) {
            avanzarTurnoDespuesDeAccion(desdeId);
        }
        
        notificarActualizacionEstado();
    }
    
    private void aplicarCartaAccion(int objetivoId, Carta carta, int desdeIdJugador) {
        Jugador objetivo = estadoJuego.getJugadorPorId(objetivoId);
        if (objetivo == null) return;
        
        switch (carta.getTipo()) {
            case CONGELAR:
                objetivo.agregarCartaAccion(carta);
                objetivo.setCongelado(true);
                notificarCartaRepartida(objetivoId, carta);
                notificarJugadorCongelado(objetivoId);
                break;
                
            case VOLTEAR_TRES:
                objetivo.agregarCartaAccion(carta);
                notificarCartaRepartida(objetivoId, carta);
                
                if (objetivoId == desdeIdJugador) {
                    iniciarVoltearTres(objetivo);
                } else {
                    iniciarVoltearTresParaOtro(objetivo, desdeIdJugador);
                }
                break;
                
            case SEGUNDA_OPORTUNIDAD:
                objetivo.setCartaSegundaOportunidad(carta);
                notificarCartaRepartida(objetivoId, carta);
                break;
        }
    }
    
    private void iniciarVoltearTres(Jugador jugador) {
        procesandoVoltear3 = true;
        idJugadorVoltear3 = jugador.getId();
        cartasRestantesVoltear3 = 3;
        continuarVoltearTres(jugador);
    }
    
    private void iniciarVoltearTresParaOtro(Jugador jugadorObjetivo, int idJugadorOriginal) {
        procesandoVoltear3 = true;
        idJugadorVoltear3 = jugadorObjetivo.getId();
        cartasRestantesVoltear3 = 3;
        continuarVoltearTres(jugadorObjetivo);
        
        if (!procesandoVoltear3) {
            avanzarTurnoDespuesDeAccion(idJugadorOriginal);
        }
    }
    
    private void continuarVoltearTres(Jugador jugador) {
        while (cartasRestantesVoltear3 > 0 && !jugador.estaEliminado() && !jugador.tieneVoltear7()) {
            Carta carta = mazo.robarCarta();
            if (carta == null) break;
            
            cartasRestantesVoltear3--;
            
            if (carta.esCartaAccion()) {
                notificarCartaRepartida(jugador.getId(), carta);
                notificarCartaAccionRobada(jugador.getId(), carta);
                
                if (carta.getTipo() == Carta.TipoCarta.SEGUNDA_OPORTUNIDAD && !jugador.tieneSegundaOportunidad()) {
                    jugador.setCartaSegundaOportunidad(carta);
                    notificarActualizacionEstado();
                    continue;
                }
                
                List<Jugador> objetivosValidos = getObjetivosValidosParaAccion(jugador, carta);
                
                if (objetivosValidos.isEmpty()) {
                    mazo.descartar(carta);
                    continue;
                }
                
                if (objetivosValidos.size() == 1) {
                    aplicarCartaAccionDuranteVoltear3(objetivosValidos.get(0).getId(), carta, jugador);
                    notificarActualizacionEstado();
                } else {
                    esperandoObjetivoAccion = true;
                    jugadorEsperandoAccion = jugador.getId();
                    cartaAccionPendiente = carta;
                    notificarNecesitaObjetivoAccion(jugador.getId(), carta, objetivosValidos);
                    return;
                }
            } else {
                boolean exito = jugador.agregarCarta(carta);
                notificarCartaRepartida(jugador.getId(), carta);
                
                if (!exito && carta.esCartaNumero()) {
                    jugador.getCartasNumero().add(carta);
                    jugador.setEliminado(true);
                    notificarJugadorEliminado(jugador.getId(), carta);
                    break;
                }
            }
            
            notificarActualizacionEstado();
        }
        
        procesandoVoltear3 = false;
        idJugadorVoltear3 = -1;
        cartasRestantesVoltear3 = 0;
        
        if (jugador.tieneVoltear7()) {
            terminarRonda();
            return;
        }
        
        avanzarTurno();
        notificarActualizacionEstado();
        actualizarInfoMazo();
    }
    
    private void aplicarCartaAccionDuranteVoltear3(int objetivoId, Carta carta, Jugador jugadorVoltear3) {
        Jugador objetivo = estadoJuego.getJugadorPorId(objetivoId);
        if (objetivo == null) return;
        
        switch (carta.getTipo()) {
            case CONGELAR:
                objetivo.agregarCartaAccion(carta);
                objetivo.setCongelado(true);
                notificarCartaRepartida(objetivoId, carta);
                notificarJugadorCongelado(objetivoId);
                break;
                
            case VOLTEAR_TRES:
                objetivo.agregarCartaAccion(carta);
                notificarCartaRepartida(objetivoId, carta);
                if (objetivoId == jugadorVoltear3.getId()) {
                    cartasRestantesVoltear3 += 3;
                } else {
                    cartasRestantesVoltear3 += 3;
                }
                break;
                
            case SEGUNDA_OPORTUNIDAD:
                objetivo.setCartaSegundaOportunidad(carta);
                notificarCartaRepartida(objetivoId, carta);
                break;
        }
    }
    
    private void avanzarTurnoDespuesDeAccion(int idJugadorAccion) {
        List<Jugador> jugadoresActivos = estadoJuego.getJugadoresActivos();
        
        if (jugadoresActivos.size() <= 1) {
            if (jugadoresActivos.size() == 1 && jugadoresActivos.get(0).getId() == idJugadorAccion) {
                estadoJuego.setIndiceJugadorActual(getIndiceJugador(idJugadorAccion));
                notificarCambioTurno();
                return;
            }
            terminarRonda();
            return;
        }
        
        avanzarTurno();
    }
    
    private int getIndiceJugador(int idJugador) {
        List<Jugador> jugadores = estadoJuego.getJugadores();
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).getId() == idJugador) return i;
        }
        return 0;
    }
    
    public void jugadorSePlanta(int idJugador) {
        if (esperandoObjetivoAccion) return;
        
        Jugador jugador = estadoJuego.getJugadorPorId(idJugador);
        Jugador jugadorActual = estadoJuego.getJugadorActual();
        
        if (jugador == null || jugadorActual == null || jugadorActual.getId() != idJugador) return;
        if (!jugador.estaActivo()) return;
        if (jugador.getCantidadCartasNumero() == 0 && jugador.getCartasModificador().isEmpty()) return;
        
        jugador.setPlantado(true);
        notificarJugadorPlantado(idJugador);
        
        if (estadoJuego.esFinRonda()) {
            terminarRonda();
            return;
        }
        
        avanzarTurno();
        notificarActualizacionEstado();
    }
    
    private void manejarEliminacion(Jugador jugador, Carta cartaEliminacion) {
        if (jugador.tieneSegundaOportunidad()) {
            mazo.descartar(jugador.usarSegundaOportunidad());
            mazo.descartar(cartaEliminacion);
            notificarCartaRepartida(jugador.getId(), cartaEliminacion);
            return;
        }
        
        jugador.getCartasNumero().add(cartaEliminacion);
        notificarCartaRepartida(jugador.getId(), cartaEliminacion);
        jugador.setEliminado(true);
        notificarJugadorEliminado(jugador.getId(), cartaEliminacion);
        
        if (estadoJuego.esFinRonda()) terminarRonda();
    }
    
    private void avanzarTurno() {
        int siguiente = getSiguienteIndiceJugadorActivo(estadoJuego.getIndiceJugadorActual());
        if (siguiente == -1) {
            terminarRonda();
            return;
        }
        estadoJuego.setIndiceJugadorActual(siguiente);
        notificarCambioTurno();
    }
    
    private int getSiguienteIndiceJugadorActivo(int actual) {
        List<Jugador> jugadores = estadoJuego.getJugadores();
        for (int i = 1; i <= jugadores.size(); i++) {
            int idx = (actual + i) % jugadores.size();
            if (jugadores.get(idx).estaActivo()) return idx;
        }
        return -1;
    }
    
    private void terminarRonda() {
        estadoJuego.setFase(EstadoJuego.Fase.FIN_RONDA);
        
        esperandoObjetivoAccion = false;
        jugadorEsperandoAccion = -1;
        cartaAccionPendiente = null;
        procesandoVoltear3 = false;
        idJugadorVoltear3 = -1;
        cartasRestantesVoltear3 = 0;
        
        for (Jugador j : estadoJuego.getJugadores()) {
            int puntajeRonda = j.calcularPuntajeRonda();
            j.agregarAPuntajeTotal(puntajeRonda);
        }
        
        int siguienteIndiceRepartidor = encontrarJugadorConMasPuntosRonda();
        if (siguienteIndiceRepartidor != -1) {
            estadoJuego.setIndiceRepartidor(siguienteIndiceRepartidor);
        } else {
            estadoJuego.setIndiceRepartidor((estadoJuego.getIndiceRepartidor() + 1) % estadoJuego.getJugadores().size());
        }
        
        for (Jugador j : estadoJuego.getJugadores()) {
            mazo.descartarTodas(j.getTodasLasCartas());
        }
        
        notificarFinRonda();
        
        if (estadoJuego.esFinJuego()) {
            terminarJuego();
            return;
        }
        
        estadoJuego.setNumeroRonda(estadoJuego.getNumeroRonda() + 1);
    }
    
    private int encontrarJugadorConMasPuntosRonda() {
        List<Jugador> jugadores = estadoJuego.getJugadores();
        int maxPuntos = -1;
        int indiceGanador = -1;
        
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            if (j.estaConectado()) {
                int puntajeRonda = j.getPuntajeRonda();
                if (puntajeRonda > maxPuntos) {
                    maxPuntos = puntajeRonda;
                    indiceGanador = i;
                }
            }
        }
        
        return indiceGanador;
    }
    
    public void iniciarSiguienteRonda() {
        if (estadoJuego.esFinJuego()) {
            terminarJuego();
            return;
        }
        iniciarRonda();
    }
    
    private void terminarJuego() {
        estadoJuego.setFase(EstadoJuego.Fase.FIN_JUEGO);
        notificarFinJuego(estadoJuego.getGanador());
    }
    
    private void actualizarInfoMazo() {
        estadoJuego.setTamanoMazo(mazo.getCartasRestantes());
    }
    
    private void notificarCartaRepartida(int id, Carta c) { for (EscuchaEventosJuego e : escuchas) e.alRepartirCarta(id, c); }
    private void notificarJugadorEliminado(int id, Carta c) { for (EscuchaEventosJuego e : escuchas) e.alEliminarJugador(id, c); }
    private void notificarJugadorPlantado(int id) { for (EscuchaEventosJuego e : escuchas) e.alPlantarseJugador(id); }
    private void notificarJugadorCongelado(int id) { for (EscuchaEventosJuego e : escuchas) e.alCongelarJugador(id); }
    private void notificarCartaAccionRobada(int id, Carta c) { for (EscuchaEventosJuego e : escuchas) e.alRobarCartaAccion(id, c); }
    private void notificarFinRonda() { for (EscuchaEventosJuego e : escuchas) e.alFinRonda(estadoJuego.getJugadores(), estadoJuego.getNumeroRonda()); }
    private void notificarFinJuego(Jugador g) { for (EscuchaEventosJuego e : escuchas) e.alFinJuego(g); }
    private void notificarCambioTurno() { Jugador c = estadoJuego.getJugadorActual(); if (c != null) for (EscuchaEventosJuego e : escuchas) e.alCambiarTurno(c.getId()); }
    private void notificarActualizacionEstado() { for (EscuchaEventosJuego e : escuchas) e.alActualizarEstado(estadoJuego); }
    private void notificarNecesitaObjetivoAccion(int id, Carta c, List<Jugador> a) { for (EscuchaEventosJuego e : escuchas) e.alNecesitarObjetivoAccion(id, c, a); }
    
    public EstadoJuego getEstadoJuego() { return estadoJuego; }
}
