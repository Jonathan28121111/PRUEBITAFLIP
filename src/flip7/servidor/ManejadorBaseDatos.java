package flip7.servidor;

import flip7.comun.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManejadorBaseDatos {
    private static final String ARCHIVO_BD = "voltear7.db";
    private Connection conexion;
    
    public ManejadorBaseDatos() { inicializarBaseDatos(); }
    
    private void inicializarBaseDatos() {
        try {
            Class.forName("org.sqlite.JDBC");
            conexion = DriverManager.getConnection("jdbc:sqlite:" + ARCHIVO_BD);
            Statement stmt = conexion.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre_usuario TEXT UNIQUE NOT NULL, contrasena TEXT NOT NULL, partidas_jugadas INTEGER DEFAULT 0, partidas_ganadas INTEGER DEFAULT 0, puntaje_total INTEGER DEFAULT 0, creado_en DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.close();
            Statement contarStmt = conexion.createStatement();
            ResultSet rs = contarStmt.executeQuery("SELECT COUNT(*) FROM usuarios");
            int cantidad = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            contarStmt.close();
            System.out.println("[BD] SQLite inicializada - " + cantidad + " usuarios");
        } catch (ClassNotFoundException e) {
            System.err.println("[BD] ERROR: Driver SQLite no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[BD] ERROR SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized Usuario registrar(String nombreUsuario, String contrasena) {
        if (conexion == null) return null;
        try {
            PreparedStatement verificarStmt = conexion.prepareStatement("SELECT id FROM usuarios WHERE LOWER(nombre_usuario) = LOWER(?)");
            verificarStmt.setString(1, nombreUsuario);
            ResultSet rs = verificarStmt.executeQuery();
            if (rs.next()) { rs.close(); verificarStmt.close(); return null; }
            rs.close();
            verificarStmt.close();
            PreparedStatement insertarStmt = conexion.prepareStatement("INSERT INTO usuarios (nombre_usuario, contrasena) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertarStmt.setString(1, nombreUsuario);
            insertarStmt.setString(2, contrasena);
            insertarStmt.executeUpdate();
            ResultSet claves = insertarStmt.getGeneratedKeys();
            int id = claves.next() ? claves.getInt(1) : -1;
            claves.close();
            insertarStmt.close();
            System.out.println("[BD] Registrado: " + nombreUsuario + " (ID: " + id + ")");
            Usuario usuario = new Usuario(nombreUsuario, contrasena);
            usuario.setId(id);
            return usuario;
        } catch (SQLException e) {
            System.err.println("[BD] Error registro: " + e.getMessage());
            return null;
        }
    }
    
    public synchronized List<Usuario> obtenerRankings(int limite) {
        List<Usuario> rankings = new ArrayList<>();
        if (conexion == null) return rankings;
        try {
            String consulta = "SELECT id, nombre_usuario, partidas_jugadas, partidas_ganadas, puntaje_total FROM usuarios ORDER BY puntaje_total DESC LIMIT ?";
            PreparedStatement stmt = conexion.prepareStatement(consulta);
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Usuario usuario = new Usuario(rs.getInt("id"), rs.getString("nombre_usuario"), rs.getInt("partidas_jugadas"), rs.getInt("partidas_ganadas"), rs.getInt("puntaje_total"));
                rankings.add(usuario);
            }
            rs.close();
            stmt.close();
            System.out.println("[BD] Rankings obtenidos: " + rankings.size() + " usuarios");
        } catch (SQLException e) {
            System.err.println("[BD] Error obteniendo rankings: " + e.getMessage());
        }
        return rankings;
    }
    
    public synchronized Usuario login(String nombreUsuario, String contrasena) {
        if (conexion == null) return null;
        try {
            PreparedStatement stmt = conexion.prepareStatement("SELECT id, nombre_usuario, contrasena, partidas_jugadas, partidas_ganadas, puntaje_total FROM usuarios WHERE LOWER(nombre_usuario) = LOWER(?)");
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String contrasenaAlmacenada = rs.getString("contrasena");
                if (contrasenaAlmacenada.equals(contrasena)) {
                    Usuario usuario = new Usuario(rs.getInt("id"), rs.getString("nombre_usuario"), rs.getInt("partidas_jugadas"), rs.getInt("partidas_ganadas"), rs.getInt("puntaje_total"));
                    rs.close();
                    stmt.close();
                    System.out.println("[BD] Login: " + nombreUsuario);
                    return usuario;
                }
            }
            rs.close();
            stmt.close();
            return null;
        } catch (SQLException e) {
            System.err.println("[BD] Error login: " + e.getMessage());
            return null;
        }
    }
    
    public synchronized boolean actualizarEstadisticas(int idUsuario, boolean gano, int puntaje) {
        if (conexion == null) return false;
        try {
            PreparedStatement stmt = conexion.prepareStatement("UPDATE usuarios SET partidas_jugadas = partidas_jugadas + 1, partidas_ganadas = partidas_ganadas + ?, puntaje_total = puntaje_total + ? WHERE id = ?");
            stmt.setInt(1, gano ? 1 : 0);
            stmt.setInt(2, puntaje);
            stmt.setInt(3, idUsuario);
            int filas = stmt.executeUpdate();
            stmt.close();
            return filas > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void cerrar() {
        try { if (conexion != null && !conexion.isClosed()) conexion.close(); } catch (SQLException e) {}
    }
}
