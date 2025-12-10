package flip7.comun;
import java.io.Serializable;
import java.util.List;

public class MensajeJuego implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum TipoMensaje { 
        LOGIN, REGISTRO, LOGIN_EXITOSO, LOGIN_FALLIDO, REGISTRO_EXITOSO, REGISTRO_FALLIDO,
        CONECTAR, DESCONECTAR, LISTO, PEDIR, PLANTARSE, ASIGNAR_CARTA_ACCION, MENSAJE_CHAT, CONECTADO, 
        CREAR_SALA, UNIRSE_SALA, SALIR_SALA, OBTENER_SALAS, LISTA_SALAS, SALA_CREADA, SALA_UNIDA, SALA_ACTUALIZADA, SALA_SALIDA, ERROR_SALA,
        JUGADOR_UNIDO, JUGADOR_SALIO, JUEGO_INICIA, RONDA_INICIA, TU_TURNO, CARTA_REPARTIDA, JUGADOR_ELIMINADO, JUGADOR_PLANTADO, JUGADOR_CONGELADO, CARTA_ACCION_ROBADA, ELEGIR_OBJETIVO_ACCION, FIN_RONDA, FIN_JUEGO, ESTADO_JUEGO, ERROR, CHAT_DIFUSION,
        OBTENER_RANKINGS, RESPUESTA_RANKINGS, JUGADORES_LISTOS
    }
    
    private TipoMensaje tipo;
    private int idJugador;
    private int idJugadorObjetivo;
    private int numeroRonda;
    private String nombreJugador;
    private String mensaje;
    private String idSala;
    private String nombreSala;
    private String nombreUsuario;
    private String contrasena; 
    private int maxJugadores;
    private boolean esEspectador;
    private Carta carta;
    private List<Jugador> jugadores;
    private EstadoJuego estadoJuego;
    private List<SalaJuego> salas;
    private SalaJuego sala;
    private Usuario usuario;
    private List<Usuario> rankings;
    private List<String> jugadoresListos;
    
    public MensajeJuego(TipoMensaje t) { tipo = t; }
    
    public static MensajeJuego login(String nombreUsuario, String contrasena) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.LOGIN);
        m.nombreUsuario = nombreUsuario;
        m.contrasena = contrasena;
        return m;
    }
    
    public static MensajeJuego registro(String nombreUsuario, String contrasena) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.REGISTRO);
        m.nombreUsuario = nombreUsuario;
        m.contrasena = contrasena;
        return m;
    }
    
    public static MensajeJuego loginExitoso(Usuario usuario) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.LOGIN_EXITOSO);
        m.usuario = usuario;
        m.nombreJugador = usuario.getNombreUsuario();
        return m;
    }
    
    public static MensajeJuego loginFallido(String razon) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.LOGIN_FALLIDO);
        m.mensaje = razon;
        return m;
    }
    
    public static MensajeJuego registroExitoso(Usuario usuario) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.REGISTRO_EXITOSO);
        m.usuario = usuario;
        m.nombreJugador = usuario.getNombreUsuario();
        return m;
    }
    
    public static MensajeJuego registroFallido(String razon) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.REGISTRO_FALLIDO);
        m.mensaje = razon;
        return m;
    }
    
    public static MensajeJuego conectar(String n) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.CONECTAR); 
        m.nombreJugador = n; 
        return m; 
    }
    
    public static MensajeJuego conectado(int id, String n) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.CONECTADO); 
        m.idJugador = id; 
        m.nombreJugador = n; 
        return m; 
    }
    
    public static MensajeJuego jugadorUnido(int id, String n) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.JUGADOR_UNIDO); 
        m.idJugador = id; 
        m.nombreJugador = n; 
        return m; 
    }
    
    public static MensajeJuego juegoInicia(List<Jugador> j) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.JUEGO_INICIA); 
        m.jugadores = j; 
        return m; 
    }
    
    public static MensajeJuego tuTurno(int id) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.TU_TURNO); 
        m.idJugador = id; 
        return m; 
    }
    
    public static MensajeJuego cartaRepartida(int id, Carta c) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.CARTA_REPARTIDA); 
        m.idJugador = id; 
        m.carta = c; 
        return m; 
    }
    
    public static MensajeJuego jugadorEliminado(int id, Carta c) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.JUGADOR_ELIMINADO); 
        m.idJugador = id; 
        m.carta = c; 
        return m; 
    }
    
    public static MensajeJuego elegirObjetivoAccion(Carta c, List<Jugador> a) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.ELEGIR_OBJETIVO_ACCION); 
        m.carta = c; 
        m.jugadores = a; 
        return m; 
    }
    
    public static MensajeJuego finRonda(List<Jugador> j, int r) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.FIN_RONDA); 
        m.jugadores = j; 
        m.numeroRonda = r; 
        return m; 
    }
    
    public static MensajeJuego finJuego(List<Jugador> j, int g) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.FIN_JUEGO); 
        m.jugadores = j; 
        m.idJugador = g; 
        return m; 
    }
    
    public static MensajeJuego error(String e) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.ERROR); 
        m.mensaje = e; 
        return m; 
    }
    
    public static MensajeJuego chat(int id, String n, String t) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.CHAT_DIFUSION); 
        m.idJugador = id; 
        m.nombreJugador = n; 
        m.mensaje = t; 
        return m; 
    }
    
    public static MensajeJuego estadoJuego(EstadoJuego s) { 
        MensajeJuego m = new MensajeJuego(TipoMensaje.ESTADO_JUEGO); 
        m.estadoJuego = s; 
        return m; 
    }
    
    public static MensajeJuego crearSala(String nombreSala, String nombreJugador, int maxJugadores) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.CREAR_SALA);
        m.nombreSala = nombreSala;
        m.nombreJugador = nombreJugador;
        m.maxJugadores = maxJugadores;
        return m;
    }
    
    public static MensajeJuego unirseSala(String idSala, String nombreJugador) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.UNIRSE_SALA);
        m.idSala = idSala;
        m.nombreJugador = nombreJugador;
        m.esEspectador = false;
        return m;
    }
    
    public static MensajeJuego unirseSalaComoEspectador(String idSala, String nombreJugador) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.UNIRSE_SALA);
        m.idSala = idSala;
        m.nombreJugador = nombreJugador;
        m.esEspectador = true;
        return m;
    }
    
    public static MensajeJuego salirSala() { 
        return new MensajeJuego(TipoMensaje.SALIR_SALA); 
    }
    
    public static MensajeJuego solicitarSalas() { 
        return new MensajeJuego(TipoMensaje.OBTENER_SALAS); 
    }
    
    public static MensajeJuego listaSalas(List<SalaJuego> listaSalas) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.LISTA_SALAS);
        m.salas = listaSalas;
        return m;
    }
    
    public static MensajeJuego salaCreada(SalaJuego sala, int idJugador) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.SALA_CREADA);
        m.sala = sala;
        m.idJugador = idJugador;
        return m;
    }
    
    public static MensajeJuego salaUnida(SalaJuego sala, int idJugador) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.SALA_UNIDA);
        m.sala = sala;
        m.idJugador = idJugador;
        return m;
    }
    
    public static MensajeJuego salaActualizada(SalaJuego sala) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.SALA_ACTUALIZADA);
        m.sala = sala;
        return m;
    }
    
    public static MensajeJuego errorSala(String error) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.ERROR_SALA);
        m.mensaje = error;
        return m;
    }
    
    public static MensajeJuego solicitarRankings() { 
        return new MensajeJuego(TipoMensaje.OBTENER_RANKINGS); 
    }
    
    public static MensajeJuego respuestaRankings(List<Usuario> rankings) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.RESPUESTA_RANKINGS);
        m.rankings = rankings;
        return m;
    }
    
    public static MensajeJuego jugadoresListos(List<String> listos) {
        MensajeJuego m = new MensajeJuego(TipoMensaje.JUGADORES_LISTOS);
        m.jugadoresListos = listos;
        return m;
    }
    
    public TipoMensaje getTipo() { return tipo; }
    public int getIdJugador() { return idJugador; }
    public void setIdJugador(int id) { idJugador = id; }
    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String n) { nombreJugador = n; }
    public Carta getCarta() { return carta; }
    public void setCarta(Carta c) { carta = c; }
    public List<Jugador> getJugadores() { return jugadores; }
    public int getIdJugadorObjetivo() { return idJugadorObjetivo; }
    public void setIdJugadorObjetivo(int id) { idJugadorObjetivo = id; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String m) { mensaje = m; }
    public int getNumeroRonda() { return numeroRonda; }
    public EstadoJuego getEstadoJuego() { return estadoJuego; }
    public String getIdSala() { return idSala; }
    public String getNombreSala() { return nombreSala; }
    public int getMaxJugadores() { return maxJugadores; }
    public boolean esEspectador() { return esEspectador; }
    public void setEspectador(boolean s) { esEspectador = s; }
    public List<SalaJuego> getSalas() { return salas; }
    public SalaJuego getSala() { return sala; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public Usuario getUsuario() { return usuario; }
    public List<Usuario> getRankings() { return rankings; }
    public List<String> getJugadoresListos() { return jugadoresListos; }
}
