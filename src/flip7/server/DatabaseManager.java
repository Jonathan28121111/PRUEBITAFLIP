package flip7.server;

import flip7.common.User;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_FILE = "flip7.db";
    private Connection connection;
    
    public DatabaseManager() {
        initDatabase();
    }
    
    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            
            Statement stmt = connection.createStatement();
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL," +
                "  password TEXT NOT NULL," +
                "  games_played INTEGER DEFAULT 0," +
                "  games_won INTEGER DEFAULT 0," +
                "  total_score INTEGER DEFAULT 0," +
                "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            stmt.close();
            
            Statement countStmt = connection.createStatement();
            ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM users");
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            countStmt.close();
            
            System.out.println("[DB] SQLite inicializada - " + count + " usuarios");
            
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ERROR: Driver SQLite no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] ERROR SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized User register(String username, String password) {
        if (connection == null) return null;
        
        try {
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT id FROM users WHERE LOWER(username) = LOWER(?)"
            );
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                rs.close();
                checkStmt.close();
                return null;
            }
            rs.close();
            checkStmt.close();
            
            PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            
            ResultSet keys = insertStmt.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close();
            insertStmt.close();
            
            System.out.println("[DB] Registrado: " + username + " (ID: " + id + ")");
            
            User user = new User(username, password);
            user.setId(id);
            return user;
            
        } catch (SQLException e) {
            System.err.println("[DB] Error registro: " + e.getMessage());
            return null;
        }
    }
    
    public synchronized User login(String username, String password) {
        if (connection == null) return null;
        
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, username, password, games_played, games_won, total_score " +
                "FROM users WHERE LOWER(username) = LOWER(?)"
            );
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPass = rs.getString("password");
                
                if (storedPass.equals(password)) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("games_played"),
                        rs.getInt("games_won"),
                        rs.getInt("total_score")
                    );
                    rs.close();
                    stmt.close();
                    System.out.println("[DB] Login: " + username);
                    return user;
                }
            }
            
            rs.close();
            stmt.close();
            return null;
            
        } catch (SQLException e) {
            System.err.println("[DB] Error login: " + e.getMessage());
            return null;
        }
    }
    
    public synchronized boolean updateStats(int userId, boolean won, int score) {
        if (connection == null) return false;
        
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE users SET games_played = games_played + 1, " +
                "games_won = games_won + ?, total_score = total_score + ? WHERE id = ?"
            );
            stmt.setInt(1, won ? 1 : 0);
            stmt.setInt(2, score);
            stmt.setInt(3, userId);
            int rows = stmt.executeUpdate();
            stmt.close();
            return rows > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {}
    }
}
