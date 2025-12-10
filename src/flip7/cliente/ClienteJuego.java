package flip7.cliente;
import flip7.comun.Jugador;
import flip7.comun.MensajeJuego;
import flip7.comun.Usuario;
import flip7.comun.SalaJuego;
import flip7.comun.Carta;
import flip7.comun.EstadoJuego;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClienteJuego {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private int idJugador = -1;
    private String nombreJugador;
    private boolean conectado;
    private String idSalaActual;
    private List<EscuchaClienteJuego> escuchas = new ArrayList<>();
    private EstadoJuego estadoJuegoActual;
    
    public interface EscuchaClienteJuego {
        void alConectar(int idJugador, String nombreJugador);
        void alDesconectar();
        void alLoginExitoso(Usuario usuario);
        void alLoginFallido(String razon);
        void alRegistroExitoso(Usuario usuario);
        void alRegistroFallido(String razon);
        void alUnirseJugador(int idJugador, String nombreJugador);
        void alSalirJugador(int idJugador, String nombreJugador);
        void alIniciarJuego(List<Jugador> jugadores);
        void alIniciarRonda(int numeroRonda);
        void alTuTurno(int idJugador);
        void alRepartirCarta(int idJugador, Carta carta);
        void alEliminarJugador(int idJugador, Carta carta);
        void alPlantarseJugador(int idJugador);
        void alCongelarJugador(int idJugador);
        void alRobarCartaAccion(int idJugador, Carta carta);
        void alElegirObjetivoAccion(Carta carta, List<Jugador> jugadoresActivos);
        void alFinRonda(List<Jugador> jugadores, int numeroRonda);
        void alFinJuego(List<Jugador> jugadores, int idGanador);
        void alActualizarEstado(EstadoJuego estado);
        void alMensajeChat(int idJugador, String nombreJugador, String mensaje);
        void alError(String mensaje);
        void alListaSalas(List<SalaJuego> salas);
        void alCrearSala(SalaJuego sala, int idJugador);
        void alUnirseSala(SalaJuego sala, int idJugador);
        void alActualizarSala(SalaJuego sala);
        void alErrorSala(String error);
        void alRecibirRankings(List<Usuario> rankings);
        void alRecibirJugadoresListos(List<String> listos);
    }
    
    public void agregarEscucha(EscuchaClienteJuego e) { escuchas.add(e); }
    
    public boolean conectar(String host, int puerto, String nombre) {
        if (conectado && socket != null && !socket.isClosed()) {
            return true;
        }
        
        try {
            nombreJugador = nombre; 
            socket = new Socket(host, puerto);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
            conectado = true;
            Thread t = new Thread(this::escuchar); 
            t.setDaemon(true); 
            t.start();
            return true;
        } catch (IOException e) { 
            conectado = false;
            for (EscuchaClienteJuego ec : escuchas) ec.alError("No se pudo conectar"); 
            return false; 
        }
    }
    
    private void escuchar() {
        try { 
            while (socket != null && !socket.isClosed() && conectado) { 
                MensajeJuego msg = (MensajeJuego) entrada.readObject(); 
                if (msg != null) manejarMensaje(msg); 
            } 
        }
        catch (Exception e) { 
            if (conectado) {
                conectado = false;
                for (EscuchaClienteJuego ec : escuchas) ec.alDesconectar(); 
            }
        }
    }
    
    private void manejarMensaje(MensajeJuego msg) {
        switch (msg.getTipo()) {
            case CONECTADO: idJugador = msg.getIdJugador(); conectado = true; for (EscuchaClienteJuego e : escuchas) e.alConectar(idJugador, nombreJugador); break;
            case LOGIN_EXITOSO: nombreJugador = msg.getNombreJugador(); for (EscuchaClienteJuego e : escuchas) e.alLoginExitoso(msg.getUsuario()); break;
            case LOGIN_FALLIDO: for (EscuchaClienteJuego e : escuchas) e.alLoginFallido(msg.getMensaje()); break;
            case REGISTRO_EXITOSO: nombreJugador = msg.getNombreJugador(); for (EscuchaClienteJuego e : escuchas) e.alRegistroExitoso(msg.getUsuario()); break;
            case REGISTRO_FALLIDO: for (EscuchaClienteJuego e : escuchas) e.alRegistroFallido(msg.getMensaje()); break;
            case LISTA_SALAS: for (EscuchaClienteJuego e : escuchas) e.alListaSalas(msg.getSalas()); break;
            case SALA_CREADA: idSalaActual = msg.getSala().getIdSala(); idJugador = msg.getIdJugador(); for (EscuchaClienteJuego e : escuchas) e.alCrearSala(msg.getSala(), msg.getIdJugador()); break;
            case SALA_UNIDA: idSalaActual = msg.getSala().getIdSala(); idJugador = msg.getIdJugador(); for (EscuchaClienteJuego e : escuchas) e.alUnirseSala(msg.getSala(), msg.getIdJugador()); break;
            case SALA_ACTUALIZADA: for (EscuchaClienteJuego e : escuchas) e.alActualizarSala(msg.getSala()); break;
            case ERROR_SALA: for (EscuchaClienteJuego e : escuchas) e.alErrorSala(msg.getMensaje()); break;
            case JUGADOR_UNIDO: for (EscuchaClienteJuego e : escuchas) e.alUnirseJugador(msg.getIdJugador(), msg.getNombreJugador()); break;
            case JUGADOR_SALIO: for (EscuchaClienteJuego e : escuchas) e.alSalirJugador(msg.getIdJugador(), msg.getNombreJugador()); break;
            case JUEGO_INICIA: for (EscuchaClienteJuego e : escuchas) e.alIniciarJuego(msg.getJugadores()); break;
            case RONDA_INICIA: for (EscuchaClienteJuego e : escuchas) e.alIniciarRonda(msg.getNumeroRonda()); break;
            case TU_TURNO: for (EscuchaClienteJuego e : escuchas) e.alTuTurno(msg.getIdJugador()); break;
            case CARTA_REPARTIDA: for (EscuchaClienteJuego e : escuchas) e.alRepartirCarta(msg.getIdJugador(), msg.getCarta()); break;
            case JUGADOR_ELIMINADO: for (EscuchaClienteJuego e : escuchas) e.alEliminarJugador(msg.getIdJugador(), msg.getCarta()); break;
            case JUGADOR_PLANTADO: for (EscuchaClienteJuego e : escuchas) e.alPlantarseJugador(msg.getIdJugador()); break;
            case JUGADOR_CONGELADO: for (EscuchaClienteJuego e : escuchas) e.alCongelarJugador(msg.getIdJugador()); break;
            case CARTA_ACCION_ROBADA: for (EscuchaClienteJuego e : escuchas) e.alRobarCartaAccion(msg.getIdJugador(), msg.getCarta()); break;
            case ELEGIR_OBJETIVO_ACCION: for (EscuchaClienteJuego e : escuchas) e.alElegirObjetivoAccion(msg.getCarta(), msg.getJugadores()); break;
            case FIN_RONDA: for (EscuchaClienteJuego e : escuchas) e.alFinRonda(msg.getJugadores(), msg.getNumeroRonda()); break;
            case FIN_JUEGO: for (EscuchaClienteJuego e : escuchas) e.alFinJuego(msg.getJugadores(), msg.getIdJugador()); break;
            case ESTADO_JUEGO: estadoJuegoActual = msg.getEstadoJuego(); for (EscuchaClienteJuego e : escuchas) e.alActualizarEstado(estadoJuegoActual); break;
            case CHAT_DIFUSION: for (EscuchaClienteJuego e : escuchas) e.alMensajeChat(msg.getIdJugador(), msg.getNombreJugador(), msg.getMensaje()); break;
            case ERROR: for (EscuchaClienteJuego e : escuchas) e.alError(msg.getMensaje()); break;
            case RESPUESTA_RANKINGS: for (EscuchaClienteJuego e : escuchas) e.alRecibirRankings(msg.getRankings()); break;
            case JUGADORES_LISTOS: for (EscuchaClienteJuego e : escuchas) e.alRecibirJugadoresListos(msg.getJugadoresListos()); break;
        }
    }
    
    public void enviarMensaje(MensajeJuego m) { 
        if (socket == null || socket.isClosed() || salida == null) return; 
        try { 
            synchronized (salida) { 
                salida.writeObject(m); 
                salida.flush(); 
                salida.reset(); 
            } 
        } catch (IOException e) {} 
    }
    
    public void login(String usuario, String contrasena) { enviarMensaje(MensajeJuego.login(usuario, contrasena)); }
    public void registrar(String usuario, String contrasena) { enviarMensaje(MensajeJuego.registro(usuario, contrasena)); }
    
    public void solicitarSalas() { enviarMensaje(MensajeJuego.solicitarSalas()); }
    public void crearSala(String nombreSala, int maxJugadores) { enviarMensaje(MensajeJuego.crearSala(nombreSala, nombreJugador, maxJugadores)); }
    public void unirseSala(String idSala) { enviarMensaje(MensajeJuego.unirseSala(idSala, nombreJugador)); }
    public void unirseSalaComoEspectador(String idSala) { enviarMensaje(MensajeJuego.unirseSalaComoEspectador(idSala, nombreJugador)); }
    public void salirSala() { enviarMensaje(MensajeJuego.salirSala()); idSalaActual = null; }
    
    public void listo() { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.LISTO); m.setIdJugador(idJugador); enviarMensaje(m); }
    public void pedir() { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.PEDIR); m.setIdJugador(idJugador); enviarMensaje(m); }
    public void plantarse() { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.PLANTARSE); m.setIdJugador(idJugador); enviarMensaje(m); }
    public void asignarCartaAccion(int idObjetivo, Carta carta) { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.ASIGNAR_CARTA_ACCION); m.setIdJugador(idJugador); m.setIdJugadorObjetivo(idObjetivo); m.setCarta(carta); enviarMensaje(m); }
    public void enviarChat(String texto) { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.MENSAJE_CHAT); m.setIdJugador(idJugador); m.setNombreJugador(nombreJugador); m.setMensaje(texto); enviarMensaje(m); }
    public void desconectar() { if (!conectado) return; MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.DESCONECTAR); m.setIdJugador(idJugador); enviarMensaje(m); try { if (socket != null) socket.close(); } catch (IOException e) {} conectado = false; }
    
    public int getIdJugador() { return idJugador; }
    public void setNombreJugador(String nombre) { this.nombreJugador = nombre; }
    public String getNombreJugador() { return nombreJugador; }
    public boolean estaConectado() { return conectado; }
    public String getIdSalaActual() { return idSalaActual; }
    public boolean estaEnSala() { return idSalaActual != null; }
    public EstadoJuego getEstadoJuegoActual() { return estadoJuegoActual; }
    public void solicitarRankings() { enviarMensaje(MensajeJuego.solicitarRankings()); }
}
