package flip7.common;

import java.io.Serializable;
import java.util.*;

public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    private String roomName;
    private String hostName;
    private int hostId;
    private int maxPlayers;
    private List<String> playerNames = new ArrayList<>();
    private List<String> spectatorNames = new ArrayList<>();
    private boolean gameStarted;
    private long createdTime;
    
    public GameRoom(String roomId, String roomName, String hostName, int hostId, int maxPlayers) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostName = hostName;
        this.hostId = hostId;
        this.maxPlayers = maxPlayers;
        this.createdTime = System.currentTimeMillis();
        this.playerNames.add(hostName);
    }
    
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getHostName() { return hostName; }
    public int getHostId() { return hostId; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return playerNames.size(); }
    public int getSpectatorCount() { return spectatorNames.size(); }
    public List<String> getPlayerNames() { return new ArrayList<>(playerNames); }
    public List<String> getSpectatorNames() { return new ArrayList<>(spectatorNames); }
    public boolean isGameStarted() { return gameStarted; }
    public void setGameStarted(boolean started) { this.gameStarted = started; }
    public long getCreatedTime() { return createdTime; }
    
    public boolean isFull() { return playerNames.size() >= maxPlayers; }
    
    public void addPlayer(String name) {
        if (!isFull() && !playerNames.contains(name)) {
            playerNames.add(name);
        }
    }
    
    public void addSpectator(String name) {
        if (!spectatorNames.contains(name)) {
            spectatorNames.add(name);
        }
    }
    
    public void removePlayer(String name) {
        playerNames.remove(name);
    }
    
    public void removeSpectator(String name) {
        spectatorNames.remove(name);
    }
    
    public boolean isSpectator(String name) {
        return spectatorNames.contains(name);
    }
    
    @Override
    public String toString() {
        return roomName + " (" + getCurrentPlayers() + "/" + maxPlayers + ")";
    }
}
