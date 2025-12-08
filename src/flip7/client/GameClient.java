package flip7.client;
import flip7.common.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class GameClient {
    private Socket socket; private ObjectOutputStream out; private ObjectInputStream in;
    private int playerId = -1; private String playerName; private boolean connected;
    private String currentRoomId;
    private List<GameClientListener> listeners = new ArrayList<>(); private GameState currentGameState;
    
    public interface GameClientListener {
        void onConnected(int playerId, String playerName); void onDisconnected();
        void onLoginSuccess(User user); void onLoginFailed(String reason);
        void onRegisterSuccess(User user); void onRegisterFailed(String reason);
        void onPlayerJoined(int playerId, String playerName); void onPlayerLeft(int playerId, String playerName);
        void onGameStart(List<Player> players); void onRoundStart(int roundNumber); void onYourTurn(int playerId);
        void onCardDealt(int playerId, Card card); void onPlayerBusted(int playerId, Card card);
        void onPlayerStand(int playerId); void onPlayerFrozen(int playerId);
        void onActionCardDrawn(int playerId, Card card); void onChooseActionTarget(Card card, List<Player> activePlayers);
        void onRoundEnd(List<Player> players, int roundNumber); void onGameEnd(List<Player> players, int winnerId);
        void onGameStateUpdate(GameState state); void onChatMessage(int playerId, String playerName, String message);
        void onError(String message);
        // Nuevos métodos para salas
        void onRoomList(List<GameRoom> rooms);
        void onRoomCreated(GameRoom room, int playerId);
        void onRoomJoined(GameRoom room, int playerId);
        void onRoomUpdate(GameRoom room);
        void onRoomError(String error);
        void onRankingsReceived(List<User> rankings);
    }
    
    public void addListener(GameClientListener l) { listeners.add(l); }
    
    public boolean connect(String host, int port, String name) {
        if (connected && socket != null && !socket.isClosed()) {
            return true; // Ya conectado
        }
        
        try {
            playerName = name; 
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true; // Marcar como conectado inmediatamente
            Thread t = new Thread(this::listen); 
            t.setDaemon(true); 
            t.start();
            return true;
        } catch (IOException e) { 
            connected = false;
            for (GameClientListener l : listeners) l.onError("No se pudo conectar"); 
            return false; 
        }
    }
    
    private void listen() {
        try { 
            while (socket != null && !socket.isClosed() && connected) { 
                GameMessage msg = (GameMessage) in.readObject(); 
                if (msg != null) handleMessage(msg); 
            } 
        }
        catch (Exception e) { 
            if (connected) {
                connected = false;
                for (GameClientListener l : listeners) l.onDisconnected(); 
            }
        }
    }
    
    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case CONNECTED: playerId = msg.getPlayerId(); connected = true; for (GameClientListener l : listeners) l.onConnected(playerId, playerName); break;
            case LOGIN_SUCCESS: playerName = msg.getPlayerName(); for (GameClientListener l : listeners) l.onLoginSuccess(msg.getUser()); break;
            case LOGIN_FAILED: for (GameClientListener l : listeners) l.onLoginFailed(msg.getMessage()); break;
            case REGISTER_SUCCESS: playerName = msg.getPlayerName(); for (GameClientListener l : listeners) l.onRegisterSuccess(msg.getUser()); break;
            case REGISTER_FAILED: for (GameClientListener l : listeners) l.onRegisterFailed(msg.getMessage()); break;
            case ROOM_LIST: for (GameClientListener l : listeners) l.onRoomList(msg.getRooms()); break;
            case ROOM_CREATED: currentRoomId = msg.getRoom().getRoomId(); playerId = msg.getPlayerId(); for (GameClientListener l : listeners) l.onRoomCreated(msg.getRoom(), msg.getPlayerId()); break;
            case ROOM_JOINED: currentRoomId = msg.getRoom().getRoomId(); playerId = msg.getPlayerId(); for (GameClientListener l : listeners) l.onRoomJoined(msg.getRoom(), msg.getPlayerId()); break;
            case ROOM_UPDATE: for (GameClientListener l : listeners) l.onRoomUpdate(msg.getRoom()); break;
            case ROOM_ERROR: for (GameClientListener l : listeners) l.onRoomError(msg.getMessage()); break;
            case PLAYER_JOINED: for (GameClientListener l : listeners) l.onPlayerJoined(msg.getPlayerId(), msg.getPlayerName()); break;
            case PLAYER_LEFT: for (GameClientListener l : listeners) l.onPlayerLeft(msg.getPlayerId(), msg.getPlayerName()); break;
            case GAME_START: for (GameClientListener l : listeners) l.onGameStart(msg.getPlayers()); break;
            case ROUND_START: for (GameClientListener l : listeners) l.onRoundStart(msg.getRoundNumber()); break;
            case YOUR_TURN: for (GameClientListener l : listeners) l.onYourTurn(msg.getPlayerId()); break;
            case CARD_DEALT: for (GameClientListener l : listeners) l.onCardDealt(msg.getPlayerId(), msg.getCard()); break;
            case PLAYER_BUSTED: for (GameClientListener l : listeners) l.onPlayerBusted(msg.getPlayerId(), msg.getCard()); break;
            case PLAYER_STAND: for (GameClientListener l : listeners) l.onPlayerStand(msg.getPlayerId()); break;
            case PLAYER_FROZEN: for (GameClientListener l : listeners) l.onPlayerFrozen(msg.getPlayerId()); break;
            case ACTION_CARD_DRAWN: for (GameClientListener l : listeners) l.onActionCardDrawn(msg.getPlayerId(), msg.getCard()); break;
            case CHOOSE_ACTION_TARGET: for (GameClientListener l : listeners) l.onChooseActionTarget(msg.getCard(), msg.getPlayers()); break;
            case ROUND_END: for (GameClientListener l : listeners) l.onRoundEnd(msg.getPlayers(), msg.getRoundNumber()); break;
            case GAME_END: for (GameClientListener l : listeners) l.onGameEnd(msg.getPlayers(), msg.getPlayerId()); break;
            case GAME_STATE: currentGameState = msg.getGameState(); for (GameClientListener l : listeners) l.onGameStateUpdate(currentGameState); break;
            case CHAT_BROADCAST: for (GameClientListener l : listeners) l.onChatMessage(msg.getPlayerId(), msg.getPlayerName(), msg.getMessage()); break;
            case ERROR: for (GameClientListener l : listeners) l.onError(msg.getMessage()); break;
            case RANKINGS_RESPONSE: 
    for (GameClientListener l : listeners) l.onRankingsReceived(msg.getRankings()); 
    break;

        }
    }
    
    public void sendMessage(GameMessage m) { 
        if (socket == null || socket.isClosed() || out == null) return; 
        try { 
            synchronized (out) { 
                out.writeObject(m); 
                out.flush(); 
                out.reset(); 
            } 
        } catch (IOException e) {
            // Error al enviar
        } 
    }
    
    // Autenticación
    public void login(String username, String password) { sendMessage(GameMessage.login(username, password)); }
    public void register(String username, String password) { sendMessage(GameMessage.register(username, password)); }
    
    // Métodos para salas
    public void requestRooms() { sendMessage(GameMessage.requestRooms()); }
    public void createRoom(String roomName, int maxPlayers) { sendMessage(GameMessage.createRoom(roomName, playerName, maxPlayers)); }
    public void joinRoom(String roomId) { sendMessage(GameMessage.joinRoom(roomId, playerName)); }
    public void joinRoomAsSpectator(String roomId) { sendMessage(GameMessage.joinRoomAsSpectator(roomId, playerName)); }
    public void leaveRoom() { sendMessage(GameMessage.leaveRoom()); currentRoomId = null; }
    
    // Métodos de juego
    public void ready() { GameMessage m = new GameMessage(GameMessage.MessageType.READY); m.setPlayerId(playerId); sendMessage(m); }
    public void hit() { GameMessage m = new GameMessage(GameMessage.MessageType.HIT); m.setPlayerId(playerId); sendMessage(m); }
    public void stand() { GameMessage m = new GameMessage(GameMessage.MessageType.STAND); m.setPlayerId(playerId); sendMessage(m); }
    public void assignActionCard(int targetId, Card card) { GameMessage m = new GameMessage(GameMessage.MessageType.ASSIGN_ACTION_CARD); m.setPlayerId(playerId); m.setTargetPlayerId(targetId); m.setCard(card); sendMessage(m); }
    public void sendChat(String txt) { GameMessage m = new GameMessage(GameMessage.MessageType.CHAT_MESSAGE); m.setPlayerId(playerId); m.setPlayerName(playerName); m.setMessage(txt); sendMessage(m); }
    public void disconnect() { if (!connected) return; GameMessage m = new GameMessage(GameMessage.MessageType.DISCONNECT); m.setPlayerId(playerId); sendMessage(m); try { if (socket != null) socket.close(); } catch (IOException e) {} connected = false; }
    
    public int getPlayerId() { return playerId; }
    public void setPlayerName(String name) { this.playerName = name; }
    public String getPlayerName() { return playerName; }
    public boolean isConnected() { return connected; }
    public String getCurrentRoomId() { return currentRoomId; }
    public boolean isInRoom() { return currentRoomId != null; }
    public GameState getCurrentGameState() { return currentGameState; }
    public void requestRankings() { 
    sendMessage(GameMessage.requestRankings()); 
}
}
