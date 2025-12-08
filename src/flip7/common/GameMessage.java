package flip7.common;
import java.io.Serializable;
import java.util.List;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum MessageType { 
        // Autenticación
        LOGIN, REGISTER, LOGIN_SUCCESS, LOGIN_FAILED, REGISTER_SUCCESS, REGISTER_FAILED,
        // Conexión básica
        CONNECT, DISCONNECT, READY, HIT, STAND, ASSIGN_ACTION_CARD, CHAT_MESSAGE, CONNECTED, 
        // Lobby/Salas
        CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM, GET_ROOMS, ROOM_LIST, ROOM_CREATED, ROOM_JOINED, ROOM_UPDATE, ROOM_LEFT, ROOM_ERROR,
        // Juego
        PLAYER_JOINED, PLAYER_LEFT, GAME_START, ROUND_START, YOUR_TURN, CARD_DEALT, PLAYER_BUSTED, PLAYER_STAND, PLAYER_FROZEN, ACTION_CARD_DRAWN, CHOOSE_ACTION_TARGET, ROUND_END, GAME_END, GAME_STATE, ERROR, CHAT_BROADCAST,
        GET_RANKINGS, RANKINGS_RESPONSE

    }
    
    private MessageType type; private int playerId, targetPlayerId, roundNumber;
    private String playerName, message, roomId, roomName, username, password; 
    private int maxPlayers;
    private boolean isSpectator;
    private Card card; private List<Player> players; private GameState gameState;
    private List<GameRoom> rooms;
    private GameRoom room;
    private User user;
    
    public GameMessage(MessageType t) { type = t; }
    
    // Autenticación
    public static GameMessage login(String username, String password) {
        GameMessage m = new GameMessage(MessageType.LOGIN);
        m.username = username; m.password = password;
        return m;
    }
    public static GameMessage register(String username, String password) {
        GameMessage m = new GameMessage(MessageType.REGISTER);
        m.username = username; m.password = password;
        return m;
    }
    public static GameMessage loginSuccess(User user) {
        GameMessage m = new GameMessage(MessageType.LOGIN_SUCCESS);
        m.user = user; m.playerName = user.getUsername();
        return m;
    }
    public static GameMessage loginFailed(String reason) {
        GameMessage m = new GameMessage(MessageType.LOGIN_FAILED);
        m.message = reason;
        return m;
    }
    public static GameMessage registerSuccess(User user) {
        GameMessage m = new GameMessage(MessageType.REGISTER_SUCCESS);
        m.user = user; m.playerName = user.getUsername();
        return m;
    }
    public static GameMessage registerFailed(String reason) {
        GameMessage m = new GameMessage(MessageType.REGISTER_FAILED);
        m.message = reason;
        return m;
    }
    
    public static GameMessage connect(String n) { GameMessage m = new GameMessage(MessageType.CONNECT); m.playerName = n; return m; }
    public static GameMessage connected(int id, String n) { GameMessage m = new GameMessage(MessageType.CONNECTED); m.playerId = id; m.playerName = n; return m; }
    public static GameMessage playerJoined(int id, String n) { GameMessage m = new GameMessage(MessageType.PLAYER_JOINED); m.playerId = id; m.playerName = n; return m; }
    public static GameMessage gameStart(List<Player> p) { GameMessage m = new GameMessage(MessageType.GAME_START); m.players = p; return m; }
    public static GameMessage yourTurn(int id) { GameMessage m = new GameMessage(MessageType.YOUR_TURN); m.playerId = id; return m; }
    public static GameMessage cardDealt(int id, Card c) { GameMessage m = new GameMessage(MessageType.CARD_DEALT); m.playerId = id; m.card = c; return m; }
    public static GameMessage playerBusted(int id, Card c) { GameMessage m = new GameMessage(MessageType.PLAYER_BUSTED); m.playerId = id; m.card = c; return m; }
    public static GameMessage chooseActionTarget(Card c, List<Player> a) { GameMessage m = new GameMessage(MessageType.CHOOSE_ACTION_TARGET); m.card = c; m.players = a; return m; }
    public static GameMessage roundEnd(List<Player> p, int r) { GameMessage m = new GameMessage(MessageType.ROUND_END); m.players = p; m.roundNumber = r; return m; }
    public static GameMessage gameEnd(List<Player> p, int w) { GameMessage m = new GameMessage(MessageType.GAME_END); m.players = p; m.playerId = w; return m; }
    public static GameMessage error(String e) { GameMessage m = new GameMessage(MessageType.ERROR); m.message = e; return m; }
    public static GameMessage chat(int id, String n, String t) { GameMessage m = new GameMessage(MessageType.CHAT_BROADCAST); m.playerId = id; m.playerName = n; m.message = t; return m; }
    public static GameMessage gameState(GameState s) { GameMessage m = new GameMessage(MessageType.GAME_STATE); m.gameState = s; return m; }
    
    // Mensajes de sala
    public static GameMessage createRoom(String roomName, String playerName, int maxPlayers) {
        GameMessage m = new GameMessage(MessageType.CREATE_ROOM);
        m.roomName = roomName; m.playerName = playerName; m.maxPlayers = maxPlayers;
        return m;
    }
    public static GameMessage joinRoom(String roomId, String playerName) {
        GameMessage m = new GameMessage(MessageType.JOIN_ROOM);
        m.roomId = roomId; m.playerName = playerName; m.isSpectator = false;
        return m;
    }
    public static GameMessage joinRoomAsSpectator(String roomId, String playerName) {
        GameMessage m = new GameMessage(MessageType.JOIN_ROOM);
        m.roomId = roomId; m.playerName = playerName; m.isSpectator = true;
        return m;
    }
    public static GameMessage leaveRoom() { return new GameMessage(MessageType.LEAVE_ROOM); }
    public static GameMessage requestRooms() { return new GameMessage(MessageType.GET_ROOMS); }
    public static GameMessage roomList(List<GameRoom> roomsList) {
        GameMessage m = new GameMessage(MessageType.ROOM_LIST);
        m.rooms = roomsList;
        return m;
    }
    public static GameMessage roomCreated(GameRoom room, int playerId) {
        GameMessage m = new GameMessage(MessageType.ROOM_CREATED);
        m.room = room; m.playerId = playerId;
        return m;
    }
    public static GameMessage roomJoined(GameRoom room, int playerId) {
        GameMessage m = new GameMessage(MessageType.ROOM_JOINED);
        m.room = room; m.playerId = playerId;
        return m;
    }
    public static GameMessage roomUpdate(GameRoom room) {
        GameMessage m = new GameMessage(MessageType.ROOM_UPDATE);
        m.room = room;
        return m;
    }
    public static GameMessage roomError(String error) {
        GameMessage m = new GameMessage(MessageType.ROOM_ERROR);
        m.message = error;
        return m;
    }
    private List<User> rankings;

public static GameMessage requestRankings() { 
    return new GameMessage(MessageType.GET_RANKINGS); 
}

public static GameMessage rankingsResponse(List<User> rankings) {
    GameMessage m = new GameMessage(MessageType.RANKINGS_RESPONSE);
    m.rankings = rankings;
    return m;
}

public List<User> getRankings() { return rankings; }
    public MessageType getType() { return type; }
    public int getPlayerId() { return playerId; } public void setPlayerId(int id) { playerId = id; }
    public String getPlayerName() { return playerName; } public void setPlayerName(String n) { playerName = n; }
    public Card getCard() { return card; } public void setCard(Card c) { card = c; }
    public List<Player> getPlayers() { return players; }
    public int getTargetPlayerId() { return targetPlayerId; } public void setTargetPlayerId(int id) { targetPlayerId = id; }
    public String getMessage() { return message; } public void setMessage(String m) { message = m; }
    public int getRoundNumber() { return roundNumber; }
    public GameState getGameState() { return gameState; }
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isSpectator() { return isSpectator; }
    public void setSpectator(boolean s) { isSpectator = s; }
    public List<GameRoom> getRooms() { return rooms; }
    public GameRoom getRoom() { return room; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public User getUser() { return user; }
}
