package flip7.servidor;

import flip7.comun.Jugador;
import flip7.comun.MensajeJuego;
import flip7.comun.Usuario;
import flip7.comun.Carta;
import flip7.comun.SalaJuego;
import flip7.comun.EstadoJuego;
import flip7.juego.LogicaJuego;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorJuego {
    private static final int PUERTO = 5555;
    private ServerSocket servidorSocket;
    private Map<Integer, ManejadorCliente> todosClientes = new ConcurrentHashMap<>();
    private Map<String, InstanciaSalaJuego> salas = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ManejadorBaseDatos baseDatos;
    private boolean ejecutando;
    private int siguienteIdCliente = 0;
    
    public void iniciar() {
        try {
            baseDatos = new ManejadorBaseDatos();
            servidorSocket = new ServerSocket(PUERTO);
            ejecutando = true;
            System.out.println("========================================");
            System.out.println("   SERVIDOR VOLTEAR 7 - Puerto " + PUERTO);
            System.out.println("   SQLite + Sistema de Espectadores");
            System.out.println("========================================");
            while (ejecutando) {
                try {
                    Socket cliente = servidorSocket.accept();
                    System.out.println("+ Conexion: " + cliente.getInetAddress());
                    executor.execute(new ManejadorCliente(cliente, this, siguienteIdCliente++));
                } catch (IOException e) {
                    if (ejecutando) e.printStackTrace();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public synchronized void manejarLogin(ManejadorCliente manejador, String nombreUsuario, String contrasena) {
        nombreUsuario = nombreUsuario.trim();
        if (!esUsuarioValido(nombreUsuario)) {
            manejador.enviarMensaje(MensajeJuego.loginFallido("Usuario invalido.\nSolo letras y numeros.\nSin espacios.\n3 a 15 caracteres."));
            return;
        }
        if (usuarioYaConectado(nombreUsuario)) {
            manejador.enviarMensaje(MensajeJuego.loginFallido("Este usuario ya esta conectado"));
            System.out.println("[LOGIN BLOQUEADO] " + nombreUsuario + " duplicado");
            return;
        }
        Usuario usuario = baseDatos.login(nombreUsuario, contrasena);
        if (usuario != null) {
            manejador.setNombreJugador(nombreUsuario);
            manejador.setIdUsuario(usuario.getId());
            manejador.enviarMensaje(MensajeJuego.loginExitoso(usuario));
            enviarListaSalas(manejador.getIdCliente());
            System.out.println("[LOGIN] " + nombreUsuario + " conectado (ID: " + usuario.getId() + ")");
        } else {
            manejador.enviarMensaje(MensajeJuego.loginFallido("Usuario o contrasena incorrectos"));
        }
    }
    
    public void manejarRegistro(ManejadorCliente manejador, String nombreUsuario, String contrasena) {
        if (!esUsuarioValido(nombreUsuario)) {
            manejador.enviarMensaje(MensajeJuego.registroFallido("Usuario invalido.\nSolo letras y numeros.\nSin espacios.\n3 a 15 caracteres."));
            return;
        }
        if (contrasena == null || contrasena.length() < 4) {
            manejador.enviarMensaje(MensajeJuego.registroFallido("Contrasena: minimo 4 caracteres"));
            return;
        }
        Usuario usuario = baseDatos.registrar(nombreUsuario.trim(), contrasena);
        if (usuario != null) {
            manejador.setNombreJugador(nombreUsuario);
            manejador.setIdUsuario(usuario.getId());
            manejador.enviarMensaje(MensajeJuego.registroExitoso(usuario));
            enviarListaSalas(manejador.getIdCliente());
            System.out.println("[REGISTRO] " + nombreUsuario + " registrado (ID: " + usuario.getId() + ")");
        } else {
            manejador.enviarMensaje(MensajeJuego.registroFallido("El usuario ya existe"));
        }
    }
    
    public void registrarCliente(ManejadorCliente manejador) { todosClientes.put(manejador.getIdCliente(), manejador); }
    public void desregistrarCliente(int idCliente) { ManejadorCliente manejador = todosClientes.remove(idCliente); if (manejador != null) { String idSala = manejador.getIdSalaActual(); if (idSala != null) salirSala(idCliente, idSala); } }
    public boolean usuarioYaConectado(String nombreUsuario) { for (ManejadorCliente m : todosClientes.values()) if (m.getNombreJugador() != null && m.getNombreJugador().equalsIgnoreCase(nombreUsuario)) return true; return false; }
    private boolean esUsuarioValido(String nombreUsuario) { if (nombreUsuario == null) return false; if (nombreUsuario.contains(" ")) return false; if (!nombreUsuario.matches("^[a-zA-Z0-9]{3,15}$")) return false; return true; }
    public List<SalaJuego> obtenerTodasLasSalas() { List<SalaJuego> todas = new ArrayList<>(); for (InstanciaSalaJuego sala : salas.values()) todas.add(sala.getSala()); return todas; }
    
    public SalaJuego crearSala(int idCliente, String nombreSala, String nombreJugador, int maxJugadores) {
        ManejadorCliente manejador = todosClientes.get(idCliente);
        if (manejador == null) return null;
        if (manejador.getIdSalaActual() != null) { manejador.enviarMensaje(MensajeJuego.errorSala("Ya estas en una sala")); return null; }
        String idSala = UUID.randomUUID().toString().substring(0, 8);
        SalaJuego sala = new SalaJuego(idSala, nombreSala, nombreJugador, idCliente, maxJugadores);
        InstanciaSalaJuego instancia = new InstanciaSalaJuego(sala, this);
        salas.put(idSala, instancia);
        int idJugador = instancia.agregarJugador(manejador, nombreJugador);
        manejador.setIdSalaActual(idSala);
        manejador.setIdJugador(idJugador);
        manejador.setEspectador(false);
        System.out.println("[SALA] Creada: " + nombreSala + " [" + idSala + "] por " + nombreJugador);
        difundirListaSalas();
        return sala;
    }
    
    public void enviarRankings(int idCliente) { ManejadorCliente manejador = todosClientes.get(idCliente); if (manejador != null) { List<Usuario> rankings = baseDatos.obtenerRankings(100); manejador.enviarMensaje(MensajeJuego.respuestaRankings(rankings)); } }
    
    public boolean unirseSala(int idCliente, String idSala, String nombreJugador, boolean comoEspectador) {
        ManejadorCliente manejador = todosClientes.get(idCliente);
        InstanciaSalaJuego instancia = salas.get(idSala);
        if (manejador == null || instancia == null) { if (manejador != null) manejador.enviarMensaje(MensajeJuego.errorSala("Sala no encontrada")); return false; }
        if (manejador.getIdSalaActual() != null) { manejador.enviarMensaje(MensajeJuego.errorSala("Ya estas en una sala")); return false; }
        SalaJuego sala = instancia.getSala();
        if (comoEspectador) {
            instancia.agregarEspectador(manejador, nombreJugador);
            manejador.setIdSalaActual(idSala);
            manejador.setIdJugador(-1);
            manejador.setEspectador(true);
            System.out.println("[ESPEC] " + nombreJugador + " observando [" + idSala + "]");
            manejador.enviarMensaje(MensajeJuego.salaUnida(sala, -1));
            instancia.difundirActualizacionSala();
            difundirListaSalas();
            if (sala.isJuegoIniciado()) {
                manejador.enviarMensaje(MensajeJuego.juegoInicia(instancia.getLogicaJuego().getEstadoJuego().getJugadores()));
                manejador.enviarMensaje(MensajeJuego.estadoJuego(instancia.getLogicaJuego().getEstadoJuego()));
            }
            return true;
        } else {
            if (sala.estaLlena()) { manejador.enviarMensaje(MensajeJuego.errorSala("Sala llena")); return false; }
            if (sala.isJuegoIniciado()) { manejador.enviarMensaje(MensajeJuego.errorSala("Juego en curso")); return false; }
            int idJugador = instancia.agregarJugador(manejador, nombreJugador);
            manejador.setIdSalaActual(idSala);
            manejador.setIdJugador(idJugador);
            manejador.setEspectador(false);
            System.out.println("[UNIR] " + nombreJugador + " -> [" + idSala + "]");
            manejador.enviarMensaje(MensajeJuego.salaUnida(sala, idJugador));
            instancia.difundirActualizacionSala();
            difundirListaSalas();
            return true;
        }
    }
    
    public void salirSala(int idCliente, String idSala) {
        ManejadorCliente manejador = todosClientes.get(idCliente);
        InstanciaSalaJuego instancia = salas.get(idSala);
        if (manejador == null || instancia == null) return;
        if (manejador.esEspectador()) instancia.removerEspectador(idCliente);
        else instancia.removerJugador(idCliente);
        manejador.setIdSalaActual(null);
        manejador.setIdJugador(-1);
        manejador.setEspectador(false);
        if (instancia.estaVacia()) { salas.remove(idSala); System.out.println("[SALA] Eliminada: [" + idSala + "]"); }
        else instancia.difundirActualizacionSala();
        difundirListaSalas();
    }
    
    public void jugadorListo(int idCliente, String idSala) { InstanciaSalaJuego instancia = salas.get(idSala); if (instancia != null) instancia.jugadorListo(idCliente); }
    public void jugadorPide(int idCliente, String idSala) { InstanciaSalaJuego instancia = salas.get(idSala); if (instancia != null) instancia.jugadorPide(idCliente); }
    public void jugadorSePlanta(int idCliente, String idSala) { InstanciaSalaJuego instancia = salas.get(idSala); if (instancia != null) instancia.jugadorSePlanta(idCliente); }
    public void asignarCartaAccion(int idCliente, String idSala, int idObjetivo, Carta carta) { InstanciaSalaJuego instancia = salas.get(idSala); if (instancia != null) instancia.asignarCartaAccion(idCliente, idObjetivo, carta); }
    public void difundirChat(int idCliente, String idSala, String mensaje) { InstanciaSalaJuego instancia = salas.get(idSala); ManejadorCliente manejador = todosClientes.get(idCliente); if (instancia != null && manejador != null) instancia.difundirChat(manejador.getIdJugador(), manejador.getNombreJugador(), mensaje); }
    public void enviarListaSalas(int idCliente) { ManejadorCliente manejador = todosClientes.get(idCliente); if (manejador != null) manejador.enviarMensaje(MensajeJuego.listaSalas(obtenerTodasLasSalas())); }
    private void difundirListaSalas() { List<SalaJuego> todas = obtenerTodasLasSalas(); for (ManejadorCliente manejador : todosClientes.values()) if (manejador.getIdSalaActual() == null) manejador.enviarMensaje(MensajeJuego.listaSalas(todas)); }
    public void alFinJuego(String idSala) { InstanciaSalaJuego instancia = salas.get(idSala); if (instancia != null) { instancia.getSala().setJuegoIniciado(false); difundirListaSalas(); } }
    public ManejadorBaseDatos getBaseDatos() { return baseDatos; }
    
    public static void main(String[] args) { new ServidorJuego().iniciar(); }
}

class InstanciaSalaJuego implements LogicaJuego.EscuchaEventosJuego {
    private SalaJuego sala;
    private LogicaJuego logicaJuego;
    private ServidorJuego servidor;
    private Map<Integer, ManejadorCliente> jugadores = new ConcurrentHashMap<>();
    private Map<Integer, ManejadorCliente> espectadores = new ConcurrentHashMap<>();
    private Map<Integer, Integer> clienteAIdJugador = new ConcurrentHashMap<>();
    private Set<Integer> jugadoresListos = new HashSet<>();
    private Set<String> nombresJugadoresListos = new HashSet<>();
    
    public InstanciaSalaJuego(SalaJuego sala, ServidorJuego servidor) { this.sala = sala; this.servidor = servidor; this.logicaJuego = new LogicaJuego(); this.logicaJuego.agregarEscucha(this); }
    public SalaJuego getSala() { return sala; }
    public LogicaJuego getLogicaJuego() { return logicaJuego; }
    public boolean estaVacia() { return jugadores.isEmpty() && espectadores.isEmpty(); }
    
    public int agregarJugador(ManejadorCliente manejador, String nombre) {
        String nombreUnico = crearNombreUnico(nombre);
        manejador.setNombreJugador(nombreUnico);
        int idJugador = logicaJuego.agregarJugador(nombreUnico);
        jugadores.put(manejador.getIdCliente(), manejador);
        clienteAIdJugador.put(manejador.getIdCliente(), idJugador);
        sala.agregarJugador(nombreUnico);
        manejador.enviarMensaje(MensajeJuego.estadoJuego(logicaJuego.getEstadoJuego()));
        MensajeJuego msgUnir = MensajeJuego.jugadorUnido(idJugador, nombreUnico);
        for (ManejadorCliente m : jugadores.values()) if (m.getIdCliente() != manejador.getIdCliente()) m.enviarMensaje(msgUnir);
        for (ManejadorCliente m : espectadores.values()) m.enviarMensaje(msgUnir);
        return idJugador;
    }
    
    public void agregarEspectador(ManejadorCliente manejador, String nombre) {
        manejador.setNombreJugador(nombre + " (espec)");
        espectadores.put(manejador.getIdCliente(), manejador);
        sala.agregarEspectador(nombre);
        manejador.enviarMensaje(MensajeJuego.estadoJuego(logicaJuego.getEstadoJuego()));
    }
    
    private String crearNombreUnico(String nombre) {
        Set<String> existentes = new HashSet<>();
        for (Jugador j : logicaJuego.getEstadoJuego().getJugadores()) existentes.add(j.getNombre().toLowerCase());
        if (!existentes.contains(nombre.toLowerCase())) return nombre;
        int n = 2;
        while (existentes.contains((nombre + n).toLowerCase())) n++;
        return nombre + n;
    }
    
    public void removerJugador(int idCliente) {
        ManejadorCliente manejador = jugadores.remove(idCliente);
        Integer idJugador = clienteAIdJugador.remove(idCliente);
        jugadoresListos.remove(idCliente);
        if (manejador != null) nombresJugadoresListos.remove(manejador.getNombreJugador());
        if (manejador == null || idJugador == null) return;
        String nombreJugador = manejador.getNombreJugador();
        sala.removerJugador(nombreJugador);
        logicaJuego.removerJugador(idJugador);
        MensajeJuego msg = new MensajeJuego(MensajeJuego.TipoMensaje.JUGADOR_SALIO);
        msg.setIdJugador(idJugador);
        msg.setNombreJugador(nombreJugador);
        difundir(msg);
        if (sala.isJuegoIniciado() && jugadores.size() == 1) {
            ManejadorCliente ganadorManejador = jugadores.values().iterator().next();
            Integer ganadorIdJugador = null;
            for (Map.Entry<Integer, Integer> entrada : clienteAIdJugador.entrySet()) if (jugadores.containsKey(entrada.getKey())) { ganadorIdJugador = entrada.getValue(); break; }
            if (ganadorIdJugador != null) {
                Jugador ganadorJugador = encontrarJugadorPorId(ganadorIdJugador);
                Jugador salioJugador = encontrarJugadorPorId(idJugador);
                if (ganadorJugador != null) {
                    System.out.println("[JUEGO] " + ganadorJugador.getNombre() + " gana por abandono!");
                    difundir(MensajeJuego.finJuego(logicaJuego.getEstadoJuego().getJugadores(), ganadorIdJugador));
                    actualizarEstadisticasJugador(ganadorManejador, true, ganadorJugador);
                    actualizarEstadisticasJugador(manejador, false, salioJugador);
                    terminarJuego();
                }
            }
        } else if (sala.isJuegoIniciado() && jugadores.isEmpty()) {
            System.out.println("[JUEGO] Sala [" + sala.getIdSala() + "] sin jugadores, finalizando juego");
            terminarJuego();
        }
        enviarListaJugadoresListos();
    }
    
    private Jugador encontrarJugadorPorId(int idJugador) { for (Jugador j : logicaJuego.getEstadoJuego().getJugadores()) if (j.getId() == idJugador) return j; return null; }
    
    private void actualizarEstadisticasJugador(ManejadorCliente manejador, boolean gano, Jugador jugador) {
        if (manejador == null || jugador == null) return;
        int idUsuario = manejador.getIdUsuario();
        if (idUsuario > 0) {
            ManejadorBaseDatos bd = servidor.getBaseDatos();
            int puntaje = jugador.getPuntajeTotal();
            boolean actualizado = bd.actualizarEstadisticas(idUsuario, gano, puntaje);
            if (actualizado) System.out.println("[BD] " + (gano ? "Victoria" : "Derrota") + " registrada: " + manejador.getNombreJugador() + " (Puntaje: " + puntaje + ")");
            else System.err.println("[BD] Error actualizando stats de: " + manejador.getNombreJugador());
        }
    }
    
    private void terminarJuego() {
        sala.setJuegoIniciado(false);
        jugadoresListos.clear();
        nombresJugadoresListos.clear();
        for (Jugador j : logicaJuego.getEstadoJuego().getJugadores()) j.reiniciarParaNuevoJuego();
        servidor.alFinJuego(sala.getIdSala());
    }
    
    public void removerEspectador(int idCliente) { ManejadorCliente manejador = espectadores.remove(idCliente); if (manejador != null) sala.removerEspectador(manejador.getNombreJugador()); }
    
    public void jugadorListo(int idCliente) {
        if (sala.isJuegoIniciado()) return;
        if (!jugadores.containsKey(idCliente)) return;
        ManejadorCliente manejador = jugadores.get(idCliente);
        jugadoresListos.add(idCliente);
        if (manejador != null) nombresJugadoresListos.add(manejador.getNombreJugador());
        System.out.println("[LISTO] " + sala.getIdSala() + ": " + jugadoresListos.size() + "/" + jugadores.size());
        enviarListaJugadoresListos();
        if (jugadoresListos.size() >= 2 && jugadoresListos.size() == jugadores.size()) {
            sala.setJuegoIniciado(true);
            System.out.println("[JUEGO] Iniciando en [" + sala.getIdSala() + "]");
            difundir(MensajeJuego.juegoInicia(logicaJuego.getEstadoJuego().getJugadores()));
            logicaJuego.iniciarJuego();
        }
    }
    
    private void enviarListaJugadoresListos() {
        List<String> listaListos = new ArrayList<>(nombresJugadoresListos);
        MensajeJuego msg = MensajeJuego.jugadoresListos(listaListos);
        for (ManejadorCliente m : jugadores.values()) m.enviarMensaje(msg);
        for (ManejadorCliente m : espectadores.values()) m.enviarMensaje(msg);
    }
    
    public void jugadorPide(int idCliente) { Integer idJugador = clienteAIdJugador.get(idCliente); if (idJugador != null && sala.isJuegoIniciado()) logicaJuego.jugadorPide(idJugador); }
    public void jugadorSePlanta(int idCliente) { Integer idJugador = clienteAIdJugador.get(idCliente); if (idJugador != null && sala.isJuegoIniciado()) logicaJuego.jugadorSePlanta(idJugador); }
    public void asignarCartaAccion(int idCliente, int idJugadorObjetivo, Carta carta) { Integer idJugador = clienteAIdJugador.get(idCliente); if (idJugador != null && sala.isJuegoIniciado()) logicaJuego.asignarCartaAccion(idJugador, idJugadorObjetivo, carta); }
    public void difundirChat(int idJugador, String nombre, String mensaje) { difundir(MensajeJuego.chat(idJugador, nombre, mensaje)); }
    public void difundirActualizacionSala() { MensajeJuego msg = MensajeJuego.salaActualizada(sala); for (ManejadorCliente m : jugadores.values()) m.enviarMensaje(msg); for (ManejadorCliente m : espectadores.values()) m.enviarMensaje(msg); }
    private void difundir(MensajeJuego msg) { for (ManejadorCliente m : jugadores.values()) m.enviarMensaje(msg); for (ManejadorCliente m : espectadores.values()) m.enviarMensaje(msg); }
    private void enviarAJugador(int idJugador, MensajeJuego msg) { for (Map.Entry<Integer, Integer> entrada : clienteAIdJugador.entrySet()) if (entrada.getValue() == idJugador) { ManejadorCliente manejador = jugadores.get(entrada.getKey()); if (manejador != null) manejador.enviarMensaje(msg); break; } }
    private void difundirEstadoJuego() { difundir(MensajeJuego.estadoJuego(logicaJuego.getEstadoJuego())); }
    
    public void alRepartirCarta(int id, Carta c) { difundir(MensajeJuego.cartaRepartida(id, c)); difundirEstadoJuego(); }
    public void alEliminarJugador(int id, Carta c) { difundir(MensajeJuego.jugadorEliminado(id, c)); difundirEstadoJuego(); }
    public void alPlantarseJugador(int id) { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.JUGADOR_PLANTADO); m.setIdJugador(id); difundir(m); difundirEstadoJuego(); }
    public void alCongelarJugador(int id) { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.JUGADOR_CONGELADO); m.setIdJugador(id); difundir(m); difundirEstadoJuego(); }
    public void alRobarCartaAccion(int id, Carta c) { MensajeJuego m = new MensajeJuego(MensajeJuego.TipoMensaje.CARTA_ACCION_ROBADA); m.setIdJugador(id); m.setCarta(c); difundir(m); }
    public void alCambiarTurno(int id) { difundir(MensajeJuego.tuTurno(id)); difundirEstadoJuego(); }
    public void alActualizarEstado(EstadoJuego e) { difundirEstadoJuego(); }
    public void alNecesitarObjetivoAccion(int id, Carta c, java.util.List<Jugador> a) { enviarAJugador(id, MensajeJuego.elegirObjetivoAccion(c, a)); }
    
    public void alFinRonda(java.util.List<Jugador> jugadores, int ronda) {
        difundir(MensajeJuego.finRonda(jugadores, ronda));
        new Timer().schedule(new TimerTask() { public void run() { synchronized (InstanciaSalaJuego.this) { if (sala.isJuegoIniciado() && logicaJuego.getEstadoJuego().getFase() == EstadoJuego.Fase.FIN_RONDA) logicaJuego.iniciarSiguienteRonda(); } } }, 5000);
    }
    
    public void alFinJuego(Jugador ganador) {
        difundir(MensajeJuego.finJuego(logicaJuego.getEstadoJuego().getJugadores(), ganador.getId()));
        ManejadorBaseDatos bd = servidor.getBaseDatos();
        for (Map.Entry<Integer, ManejadorCliente> entrada : jugadores.entrySet()) {
            ManejadorCliente manejador = entrada.getValue();
            int idUsuario = manejador.getIdUsuario();
            if (idUsuario > 0) {
                Integer idJugador = clienteAIdJugador.get(manejador.getIdCliente());
                if (idJugador != null) {
                    Jugador jugador = encontrarJugadorPorId(idJugador);
                    if (jugador != null) {
                        boolean gano = (jugador.getId() == ganador.getId());
                        int puntaje = jugador.getPuntajeTotal();
                        boolean actualizado = bd.actualizarEstadisticas(idUsuario, gano, puntaje);
                        if (actualizado) System.out.println("[BD] Stats actualizadas: " + manejador.getNombreJugador() + " (Gano: " + gano + ", Puntaje: " + puntaje + ")");
                        else System.err.println("[BD] Error actualizando stats de: " + manejador.getNombreJugador());
                    }
                }
            }
        }
        terminarJuego();
    }
}
