package flip7.server;

import flip7.common.*;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int clientId;
    private int playerId = -1;
    private int userId = -1;
    private String playerName;
    private String currentRoomId;
    private boolean connected;
    private boolean isSpectator;
    
    public ClientHandler(Socket socket, GameServer server, int clientId) {
        this.socket = socket;
        this.server = server;
        this.clientId = clientId;
    }
    
    @Override
    public void run() {
        try {
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            server.registerClient(this);
            
            GameMessage welcome = new GameMessage(GameMessage.MessageType.CONNECTED);
            welcome.setPlayerId(clientId);
            sendMessage(welcome);
            
            server.sendRoomList(clientId);
            
            while (connected && !socket.isClosed()) {
                try {
                    GameMessage msg = (GameMessage) in.readObject();
                    if (msg != null) {
                        handleMessage(msg);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException e) {
            // Conexion cerrada
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case LOGIN:
                server.handleLogin(this, msg.getUsername(), msg.getPassword());
                break;
                
            case REGISTER:
                server.handleRegister(this, msg.getUsername(), msg.getPassword());
                break;
            
            case GET_ROOMS:
                server.sendRoomList(clientId);
                break;
                
            case CREATE_ROOM:
                GameRoom created = server.createRoom(clientId, msg.getRoomName(), msg.getPlayerName(), msg.getMaxPlayers());
                if (created != null) {
                    sendMessage(GameMessage.roomCreated(created, playerId));
                }
                break;
                
            case JOIN_ROOM:
                server.joinRoom(clientId, msg.getRoomId(), msg.getPlayerName(), msg.isSpectator());
                break;
                
            case LEAVE_ROOM:
                if (currentRoomId != null) {
                    server.leaveRoom(clientId, currentRoomId);
                    server.sendRoomList(clientId);
                }
                break;
                
            case READY:
                if (currentRoomId != null && !isSpectator) {
                    server.playerReady(clientId, currentRoomId);
                }
                break;
                
            case HIT:
                if (currentRoomId != null && !isSpectator) {
                    server.playerHit(clientId, currentRoomId);
                }
                break;
                
            case STAND:
                if (currentRoomId != null && !isSpectator) {
                    server.playerStand(clientId, currentRoomId);
                }
                break;
                
            case ASSIGN_ACTION_CARD:
                if (currentRoomId != null && !isSpectator) {
                    server.assignActionCard(clientId, currentRoomId, msg.getTargetPlayerId(), msg.getCard());
                }
                break;
                
            case CHAT_MESSAGE:
                if (currentRoomId != null) {
                    server.broadcastChat(clientId, currentRoomId, msg.getMessage());
                }
                break;
                
            case GET_RANKINGS:
                server.sendRankings(clientId);
                break;
                
            case DISCONNECT:
                disconnect();
                break;
        }
    }
    
    public void sendMessage(GameMessage msg) {
        if (!connected || out == null) return;
        
        synchronized (out) {
            try {
                out.writeObject(msg);
                out.flush();
                out.reset();
            } catch (IOException e) {
                disconnect();
            }
        }
    }
    
    private synchronized void disconnect() {
        if (!connected) return;
        connected = false;
        server.unregisterClient(clientId);
        try { 
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close(); 
        } catch (IOException e) {}
    }
    
    // ===== GETTERS Y SETTERS =====
    public int getClientId() { 
        return clientId; 
    }
    
    public int getPlayerId() { 
        return playerId; 
    }
    
    public void setPlayerId(int id) { 
        this.playerId = id; 
    }
    
    public int getUserId() { 
        return userId; 
    }
    
    public void setUserId(int id) { 
        this.userId = id; 
    }
    
    public String getPlayerName() { 
        return playerName; 
    }
    
    public void setPlayerName(String name) { 
        this.playerName = name; 
    }
    
    public String getCurrentRoomId() { 
        return currentRoomId; 
    }
    
    public void setCurrentRoomId(String roomId) { 
        this.currentRoomId = roomId; 
    }
    
    public boolean isConnected() { 
        return connected; 
    }
    
    public boolean isSpectator() { 
        return isSpectator; 
    }
    
    public void setSpectator(boolean spectator) { 
        this.isSpectator = spectator; 
    }
}