package flip7.servidor;

import flip7.comun.MensajeJuego;
import flip7.comun.SalaJuego;
import java.io.*;
import java.net.*;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private ServidorJuego servidor;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private int idCliente;
    private int idJugador = -1;
    private int idUsuario = -1;
    private String nombreJugador;
    private String idSalaActual;
    private boolean conectado;
    private boolean esEspectador;
    
    public ManejadorCliente(Socket socket, ServidorJuego servidor, int idCliente) {
        this.socket = socket;
        this.servidor = servidor;
        this.idCliente = idCliente;
    }
    
    public void run() {
        try {
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
            conectado = true;
            servidor.registrarCliente(this);
            MensajeJuego bienvenida = new MensajeJuego(MensajeJuego.TipoMensaje.CONECTADO);
            bienvenida.setIdJugador(idCliente);
            enviarMensaje(bienvenida);
            servidor.enviarListaSalas(idCliente);
            while (conectado && !socket.isClosed()) {
                try {
                    MensajeJuego msg = (MensajeJuego) entrada.readObject();
                    if (msg != null) manejarMensaje(msg);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException e) {
        } finally {
            desconectar();
        }
    }
    
    private void manejarMensaje(MensajeJuego msg) {
        switch (msg.getTipo()) {
            case LOGIN: servidor.manejarLogin(this, msg.getNombreUsuario(), msg.getContrasena()); break;
            case REGISTRO: servidor.manejarRegistro(this, msg.getNombreUsuario(), msg.getContrasena()); break;
            case OBTENER_SALAS: servidor.enviarListaSalas(idCliente); break;
            case CREAR_SALA: SalaJuego creada = servidor.crearSala(idCliente, msg.getNombreSala(), msg.getNombreJugador(), msg.getMaxJugadores()); if (creada != null) enviarMensaje(MensajeJuego.salaCreada(creada, idJugador)); break;
            case UNIRSE_SALA: servidor.unirseSala(idCliente, msg.getIdSala(), msg.getNombreJugador(), msg.esEspectador()); break;
            case SALIR_SALA: if (idSalaActual != null) { servidor.salirSala(idCliente, idSalaActual); servidor.enviarListaSalas(idCliente); } break;
            case LISTO: if (idSalaActual != null && !esEspectador) servidor.jugadorListo(idCliente, idSalaActual); break;
            case PEDIR: if (idSalaActual != null && !esEspectador) servidor.jugadorPide(idCliente, idSalaActual); break;
            case PLANTARSE: if (idSalaActual != null && !esEspectador) servidor.jugadorSePlanta(idCliente, idSalaActual); break;
            case ASIGNAR_CARTA_ACCION: if (idSalaActual != null && !esEspectador) servidor.asignarCartaAccion(idCliente, idSalaActual, msg.getIdJugadorObjetivo(), msg.getCarta()); break;
            case MENSAJE_CHAT: if (idSalaActual != null) servidor.difundirChat(idCliente, idSalaActual, msg.getMensaje()); break;
            case OBTENER_RANKINGS: servidor.enviarRankings(idCliente); break;
            case DESCONECTAR: desconectar(); break;
        }
    }
    
    public void enviarMensaje(MensajeJuego msg) {
        if (!conectado || salida == null) return;
        synchronized (salida) {
            try { salida.writeObject(msg); salida.flush(); salida.reset(); }
            catch (IOException e) { desconectar(); }
        }
    }
    
    private synchronized void desconectar() {
        if (!conectado) return;
        conectado = false;
        servidor.desregistrarCliente(idCliente);
        try { if (entrada != null) entrada.close(); if (salida != null) salida.close(); if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) {}
    }
    
    public int getIdCliente() { return idCliente; }
    public int getIdJugador() { return idJugador; }
    public void setIdJugador(int id) { this.idJugador = id; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int id) { this.idUsuario = id; }
    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombre) { this.nombreJugador = nombre; }
    public String getIdSalaActual() { return idSalaActual; }
    public void setIdSalaActual(String idSala) { this.idSalaActual = idSala; }
    public boolean estaConectado() { return conectado; }
    public boolean esEspectador() { return esEspectador; }
    public void setEspectador(boolean espectador) { this.esEspectador = espectador; }
}
