package flip7.server;

import flip7.common.*;
import flip7.game.GameLogic;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 5555;
    private ServerSocket serverSocket;
    private Map<Integer, ClientHandler> allClients = new ConcurrentHashMap<>();
    private Map<String, GameRoomInstance> rooms = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private DatabaseManager database;
    private boolean running;
    private int nextClientId = 0;
    
    public void start() {
        try {
            database = new DatabaseManager();
            serverSocket = new ServerSocket(PORT);
            running = true;
            
            System.out.println("========================================");
            System.out.println("   SERVIDOR FLIP 7 - Puerto " + PORT);
            System.out.println("   SQLite + Sistema de Espectadores");
            System.out.println("========================================");
            
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("+ Conexion: " + client.getInetAddress());
                    executor.execute(new ClientHandler(client, this, nextClientId++));
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 public synchronized void handleLogin(ClientHandler handler, String username, String password) {
    username = username.trim();
    // ✅ VALIDACIÓN DE FORMATO DE USUARIO
    if (!isValidUsername(username)) {
        handler.sendMessage(GameMessage.loginFailed(
            "❌ Usuario inválido.\nSolo letras y números.\nSin espacios.\n3 a 15 caracteres."
        ));
        return;
    }

    // ✅ BLOQUEO DE USUARIO DUPLICADO
    if (isUserAlreadyConnected(username)) {
        handler.sendMessage(GameMessage.loginFailed("⚠ Este usuario ya está conectado"));
        System.out.println("[LOGIN BLOQUEADO] " + username + " duplicado");
        return;
    }

    User user = database.login(username, password);

    if (user != null) {
        handler.setPlayerName(username);
        handler.setUserId(user.getId());
        handler.sendMessage(GameMessage.loginSuccess(user));
        sendRoomList(handler.getClientId());
        System.out.println("[LOGIN] " + username + " conectado (ID: " + user.getId() + ")");
    } else {
        handler.sendMessage(GameMessage.loginFailed("Usuario o password incorrectos"));
    }
}
    public void handleRegister(ClientHandler handler, String username, String password) {

    // ✅ VALIDAR FORMATO DEL USUARIO
    if (!isValidUsername(username)) {
        handler.sendMessage(GameMessage.registerFailed(
            "❌ Usuario inválido.\nSolo letras y números.\nSin espacios.\n3 a 15 caracteres."
        ));
        return;
    }

    // ✅ VALIDAR PASSWORD
    if (password == null || password.length() < 4) {
        handler.sendMessage(GameMessage.registerFailed("Password: mínimo 4 caracteres"));
        return;
    }

    User user = database.register(username.trim(), password);

    if (user != null) {
        handler.setPlayerName(username);
        handler.setUserId(user.getId());
        handler.sendMessage(GameMessage.registerSuccess(user));
        sendRoomList(handler.getClientId());
        System.out.println("[REGISTER] " + username + " registrado (ID: " + user.getId() + ")");
    } else {
        handler.sendMessage(GameMessage.registerFailed("El usuario ya existe"));
    }
}
    
    public void registerClient(ClientHandler handler) {
        allClients.put(handler.getClientId(), handler);
    }
    
    public void unregisterClient(int clientId) {
        ClientHandler handler = allClients.remove(clientId);
        if (handler != null) {
            String roomId = handler.getCurrentRoomId();
            if (roomId != null) {
                leaveRoom(clientId, roomId);
            }
        }
    }
    public boolean isUserAlreadyConnected(String username) {
    for (ClientHandler h : allClients.values()) {
        if (h.getPlayerName() != null && 
            h.getPlayerName().equalsIgnoreCase(username)) {
            return true;
        }
    }
    return false;
}
    
    private boolean isValidUsername(String username) {
    if (username == null) return false;

    // ❌ Bloquea espacios
    if (username.contains(" ")) return false;

    // ✅ Solo letras y números (sin tildes ni símbolos)
    if (!username.matches("^[a-zA-Z0-9]{3,15}$")) return false;

    return true;
}
    public List<GameRoom> getAllRooms() {
        List<GameRoom> all = new ArrayList<>();
        for (GameRoomInstance room : rooms.values()) {
            all.add(room.getRoom());
        }
        return all;
    }
    
    public GameRoom createRoom(int clientId, String roomName, String playerName, int maxPlayers) {
        ClientHandler handler = allClients.get(clientId);
        if (handler == null) return null;
        
        if (handler.getCurrentRoomId() != null) {
            handler.sendMessage(GameMessage.roomError("Ya estas en una sala"));
            return null;
        }
        
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        GameRoom room = new GameRoom(roomId, roomName, playerName, clientId, maxPlayers);
        GameRoomInstance instance = new GameRoomInstance(room, this);
        rooms.put(roomId, instance);
        
        int playerId = instance.addPlayer(handler, playerName);
        handler.setCurrentRoomId(roomId);
        handler.setPlayerId(playerId);
        handler.setSpectator(false);
        
        System.out.println("[SALA] Creada: " + roomName + " [" + roomId + "] por " + playerName);
        broadcastRoomList();
        
        return room;
    }
    
    public void sendRankings(int clientId) {
        ClientHandler handler = allClients.get(clientId);
        if (handler != null) {
            List<User> rankings = database.getRankings(100);
            handler.sendMessage(GameMessage.rankingsResponse(rankings));
        }
    }
    
    public boolean joinRoom(int clientId, String roomId, String playerName, boolean asSpectator) {
        ClientHandler handler = allClients.get(clientId);
        GameRoomInstance instance = rooms.get(roomId);
        
        if (handler == null || instance == null) {
            if (handler != null) handler.sendMessage(GameMessage.roomError("Sala no encontrada"));
            return false;
        }
        
        if (handler.getCurrentRoomId() != null) {
            handler.sendMessage(GameMessage.roomError("Ya estas en una sala"));
            return false;
        }
        
        GameRoom room = instance.getRoom();
        
        if (asSpectator) {
            instance.addSpectator(handler, playerName);
            handler.setCurrentRoomId(roomId);
            handler.setPlayerId(-1);
            handler.setSpectator(true);
            
            System.out.println("[SPEC] " + playerName + " observando [" + roomId + "]");
            handler.sendMessage(GameMessage.roomJoined(room, -1));
            instance.broadcastRoomUpdate();
            broadcastRoomList();
            
            if (room.isGameStarted()) {
                handler.sendMessage(GameMessage.gameStart(instance.getGameLogic().getGameState().getPlayers()));
                handler.sendMessage(GameMessage.gameState(instance.getGameLogic().getGameState()));
            }
            
            return true;
            
        } else {
            if (room.isFull()) {
                handler.sendMessage(GameMessage.roomError("Sala llena"));
                return false;
            }
            
            if (room.isGameStarted()) {
                handler.sendMessage(GameMessage.roomError("Juego en curso"));
                return false;
            }
            
            int playerId = instance.addPlayer(handler, playerName);
            handler.setCurrentRoomId(roomId);
            handler.setPlayerId(playerId);
            handler.setSpectator(false);
            
            System.out.println("[JOIN] " + playerName + " -> [" + roomId + "]");
            handler.sendMessage(GameMessage.roomJoined(room, playerId));
            instance.broadcastRoomUpdate();
            broadcastRoomList();
            return true;
        }
    }
    
    public void leaveRoom(int clientId, String roomId) {
        ClientHandler handler = allClients.get(clientId);
        GameRoomInstance instance = rooms.get(roomId);
        
        if (handler == null || instance == null) return;
        
        String playerName = handler.getPlayerName();
        
        if (handler.isSpectator()) {
            instance.removeSpectator(clientId);
        } else {
            instance.removePlayer(clientId);
        }
        
        handler.setCurrentRoomId(null);
        handler.setPlayerId(-1);
        handler.setSpectator(false);
        
        if (instance.isEmpty()) {
            rooms.remove(roomId);
            System.out.println("[SALA] Eliminada: [" + roomId + "]");
        } else {
            instance.broadcastRoomUpdate();
        }
        
        broadcastRoomList();
    }
    
    public void playerReady(int clientId, String roomId) {
        GameRoomInstance instance = rooms.get(roomId);
        if (instance != null) instance.playerReady(clientId);
    }
    
    public void playerHit(int clientId, String roomId) {
        GameRoomInstance instance = rooms.get(roomId);
        if (instance != null) instance.playerHit(clientId);
    }
    
    public void playerStand(int clientId, String roomId) {
        GameRoomInstance instance = rooms.get(roomId);
        if (instance != null) instance.playerStand(clientId);
    }
    
    public void assignActionCard(int clientId, String roomId, int targetId, Card card) {
        GameRoomInstance instance = rooms.get(roomId);
        if (instance != null) instance.assignActionCard(clientId, targetId, card);
    }
    
    public void broadcastChat(int clientId, String roomId, String message) {
        GameRoomInstance instance = rooms.get(roomId);
        ClientHandler handler = allClients.get(clientId);
        if (instance != null && handler != null) {
            instance.broadcastChat(handler.getPlayerId(), handler.getPlayerName(), message);
        }
    }
    
    public void sendRoomList(int clientId) {
        ClientHandler handler = allClients.get(clientId);
        if (handler != null) {
            handler.sendMessage(GameMessage.roomList(getAllRooms()));
        }
    }
    
    private void broadcastRoomList() {
        List<GameRoom> all = getAllRooms();
        for (ClientHandler handler : allClients.values()) {
            if (handler.getCurrentRoomId() == null) {
                handler.sendMessage(GameMessage.roomList(all));
            }
        }
    }
    
    public void onGameEnd(String roomId) {
        GameRoomInstance instance = rooms.get(roomId);
        if (instance != null) {
            instance.getRoom().setGameStarted(false);
            broadcastRoomList();
        }
    }
    
    public DatabaseManager getDatabase() { 
        return database; 
    }
    
    public static void main(String[] args) {
        new GameServer().start();
    }
}

class GameRoomInstance implements GameLogic.GameEventListener {
    private GameRoom room;
    private GameLogic gameLogic;
    private GameServer server;
    private Map<Integer, ClientHandler> players = new ConcurrentHashMap<>();
    private Map<Integer, ClientHandler> spectators = new ConcurrentHashMap<>();
    private Map<Integer, Integer> clientToPlayerId = new ConcurrentHashMap<>();
    private Set<Integer> readyPlayers = new HashSet<>();
    
    public GameRoomInstance(GameRoom room, GameServer server) {
        this.room = room;
        this.server = server;
        this.gameLogic = new GameLogic();
        this.gameLogic.addListener(this);
    }
    
    public GameRoom getRoom() { 
        return room; 
    }
    
    public GameLogic getGameLogic() { 
        return gameLogic; 
    }
    
    public boolean isEmpty() { 
        return players.isEmpty() && spectators.isEmpty(); 
    }
    
    public int addPlayer(ClientHandler handler, String name) {
        String uniqueName = makeUniqueName(name);
        handler.setPlayerName(uniqueName);
        
        int playerId = gameLogic.addPlayer(uniqueName);
        players.put(handler.getClientId(), handler);
        clientToPlayerId.put(handler.getClientId(), playerId);
        room.addPlayer(uniqueName);
        
        handler.sendMessage(GameMessage.gameState(gameLogic.getGameState()));
        
        GameMessage joinMsg = GameMessage.playerJoined(playerId, uniqueName);
        for (ClientHandler h : players.values()) {
            if (h.getClientId() != handler.getClientId()) {
                h.sendMessage(joinMsg);
            }
        }
        for (ClientHandler h : spectators.values()) {
            h.sendMessage(joinMsg);
        }
        
        return playerId;
    }
    
    public void addSpectator(ClientHandler handler, String name) {
        handler.setPlayerName(name + " (spec)");
        spectators.put(handler.getClientId(), handler);
        room.addSpectator(name);
        
        handler.sendMessage(GameMessage.gameState(gameLogic.getGameState()));
    }
    
    private String makeUniqueName(String name) {
        Set<String> existing = new HashSet<>();
        for (Player p : gameLogic.getGameState().getPlayers()) {
            existing.add(p.getName().toLowerCase());
        }
        if (!existing.contains(name.toLowerCase())) return name;
        int n = 2;
        while (existing.contains((name + n).toLowerCase())) n++;
        return name + n;
    }
    
   public void removePlayer(int clientId) {
    ClientHandler handler = players.remove(clientId);
    Integer playerId = clientToPlayerId.remove(clientId);
    readyPlayers.remove(clientId);
    
    if (handler == null || playerId == null) {
        return;
    }
    
    String playerName = handler.getPlayerName();
    room.removePlayer(playerName);
    gameLogic.removePlayer(playerId);
    
    GameMessage msg = new GameMessage(GameMessage.MessageType.PLAYER_LEFT);
    msg.setPlayerId(playerId);
    msg.setPlayerName(playerName);
    broadcast(msg);
    
    // CASO 1: Juego en curso con 1 jugador restante
    if (room.isGameStarted() && players.size() == 1) {
        ClientHandler winnerHandler = players.values().iterator().next();
        Integer winnerPlayerId = null;
        
        for (Map.Entry<Integer, Integer> entry : clientToPlayerId.entrySet()) {
            if (players.containsKey(entry.getKey())) {
                winnerPlayerId = entry.getValue();
                break;
            }
        }
        
        if (winnerPlayerId != null) {
            Player winnerPlayer = findPlayerById(winnerPlayerId);
            Player leaverPlayer = findPlayerById(playerId);
            
            if (winnerPlayer != null) {
                System.out.println("[GAME] " + winnerPlayer.getName() + " gana por abandono!");
                
               
                broadcast(GameMessage.gameEnd(gameLogic.getGameState().getPlayers(), winnerPlayerId));
                
                updatePlayerStats(winnerHandler, true, winnerPlayer);
                updatePlayerStats(handler, false, leaverPlayer);
                
                endGame();
            }
        }
    }
    // CASO 2: No quedan jugadores
    else if (room.isGameStarted() && players.isEmpty()) {
        System.out.println("[GAME] Sala [" + room.getRoomId() + "] sin jugadores, finalizando juego");
        endGame();
    }
}
    private Player findPlayerById(int playerId) {
        for (Player p : gameLogic.getGameState().getPlayers()) {
            if (p.getId() == playerId) {
                return p;
            }
        }
        return null;
    }
    
    private void updatePlayerStats(ClientHandler handler, boolean won, Player player) {
        if (handler == null || player == null) return;
        
        int userId = handler.getUserId();
        if (userId > 0) {
            DatabaseManager db = server.getDatabase();
            int score = player.getTotalScore();
            
            boolean updated = db.updateStats(userId, won, score);
            
            if (updated) {
                String result = won ? "Victoria" : "Derrota";
                System.out.println("[DB] " + result + " registrada: " + handler.getPlayerName() + 
                                 " (Score: " + score + ")");
            } else {
                System.err.println("[DB] Error actualizando stats de: " + handler.getPlayerName());
            }
        }
    }
    
    private void endGame() {
        room.setGameStarted(false);
        readyPlayers.clear();
        
        for (Player p : gameLogic.getGameState().getPlayers()) {
            p.resetForNewGame();
        }
        
        server.onGameEnd(room.getRoomId());
    }
    
    public void removeSpectator(int clientId) {
        ClientHandler handler = spectators.remove(clientId);
        if (handler != null) {
            room.removeSpectator(handler.getPlayerName());
        }
    }
    
    public void playerReady(int clientId) {
        if (room.isGameStarted()) return;
        if (!players.containsKey(clientId)) return;
        
        readyPlayers.add(clientId);
        System.out.println("[READY] " + room.getRoomId() + ": " + readyPlayers.size() + "/" + players.size());
        
        if (readyPlayers.size() >= 2 && readyPlayers.size() == players.size()) {
            room.setGameStarted(true);
            System.out.println("[GAME] Iniciando en [" + room.getRoomId() + "]");
            broadcast(GameMessage.gameStart(gameLogic.getGameState().getPlayers()));
            gameLogic.startGame();
        }
    }
    
    public void playerHit(int clientId) {
        Integer playerId = clientToPlayerId.get(clientId);
        if (playerId != null && room.isGameStarted()) gameLogic.playerHit(playerId);
    }
    
    public void playerStand(int clientId) {
        Integer playerId = clientToPlayerId.get(clientId);
        if (playerId != null && room.isGameStarted()) gameLogic.playerStand(playerId);
    }
    
    public void assignActionCard(int clientId, int targetPlayerId, Card card) {
        Integer playerId = clientToPlayerId.get(clientId);
        if (playerId != null && room.isGameStarted()) gameLogic.assignActionCard(playerId, targetPlayerId, card);
    }
    
    public void broadcastChat(int playerId, String name, String message) {
        broadcast(GameMessage.chat(playerId, name, message));
    }
    
    public void broadcastRoomUpdate() {
        GameMessage msg = GameMessage.roomUpdate(room);
        for (ClientHandler h : players.values()) h.sendMessage(msg);
        for (ClientHandler h : spectators.values()) h.sendMessage(msg);
    }
    
    private void broadcast(GameMessage msg) {
        for (ClientHandler h : players.values()) h.sendMessage(msg);
        for (ClientHandler h : spectators.values()) h.sendMessage(msg);
    }
    
    private void sendToPlayer(int playerId, GameMessage msg) {
        for (Map.Entry<Integer, Integer> entry : clientToPlayerId.entrySet()) {
            if (entry.getValue() == playerId) {
                ClientHandler handler = players.get(entry.getKey());
                if (handler != null) handler.sendMessage(msg);
                break;
            }
        }
    }
    
    private void broadcastGameState() { 
        broadcast(GameMessage.gameState(gameLogic.getGameState())); 
    }
    
    public void onCardDealt(int id, Card c) { 
        broadcast(GameMessage.cardDealt(id, c)); 
        broadcastGameState(); 
    }
    
    public void onPlayerBusted(int id, Card c) { 
        broadcast(GameMessage.playerBusted(id, c)); 
        broadcastGameState(); 
    }
    
    public void onPlayerStand(int id) { 
        GameMessage m = new GameMessage(GameMessage.MessageType.PLAYER_STAND); 
        m.setPlayerId(id); 
        broadcast(m); 
        broadcastGameState(); 
    }
    
    public void onPlayerFrozen(int id) { 
        GameMessage m = new GameMessage(GameMessage.MessageType.PLAYER_FROZEN); 
        m.setPlayerId(id); 
        broadcast(m); 
        broadcastGameState(); 
    }
    
    public void onActionCardDrawn(int id, Card c) { 
        GameMessage m = new GameMessage(GameMessage.MessageType.ACTION_CARD_DRAWN); 
        m.setPlayerId(id); 
        m.setCard(c); 
        broadcast(m); 
    }
    
    public void onTurnChange(int id) { 
        broadcast(GameMessage.yourTurn(id)); 
        broadcastGameState(); 
    }
    
    public void onGameStateUpdate(GameState s) { 
        broadcastGameState(); 
    }
    
    public void onNeedActionTarget(int id, Card c, java.util.List<Player> a) { 
        sendToPlayer(id, GameMessage.chooseActionTarget(c, a)); 
    }
    
    public void onRoundEnd(java.util.List<Player> players, int round) {
        broadcast(GameMessage.roundEnd(players, round));
        
        new Timer().schedule(new TimerTask() {
            public void run() {
                synchronized (GameRoomInstance.this) {
                    if (room.isGameStarted() && gameLogic.getGameState().getPhase() == GameState.Phase.ROUND_END) {
                        gameLogic.startNextRound();
                    }
                }
            }
        }, 5000);
    }
    
    public void onGameEnd(Player winner) {
        broadcast(GameMessage.gameEnd(gameLogic.getGameState().getPlayers(), winner.getId()));
        
        DatabaseManager db = server.getDatabase();
        
        for (Map.Entry<Integer, ClientHandler> entry : players.entrySet()) {
            ClientHandler handler = entry.getValue();
            int userId = handler.getUserId();
            
            if (userId > 0) {
                Integer playerId = clientToPlayerId.get(handler.getClientId());
                
                if (playerId != null) {
                    Player player = findPlayerById(playerId);
                    
                    if (player != null) {
                        boolean won = (player.getId() == winner.getId());
                       int score = player.getTotalScore();
                        
                        boolean updated = db.updateStats(userId, won, score);
                        
                        if (updated) {
                            System.out.println("[DB] Stats actualizadas: " + handler.getPlayerName() + 
                                             " (Won: " + won + ", Score: " + score + ")");
                        } else {
                            System.err.println("[DB] Error actualizando stats de: " + handler.getPlayerName());
                        }
                    }
                }
            }
        }
        
        endGame();
    }
}