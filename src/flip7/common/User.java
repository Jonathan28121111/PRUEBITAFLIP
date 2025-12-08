package flip7.common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private String password;
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalScore = 0;
    }
    
    public User(int id, String username, int gamesPlayed, int gamesWon, int totalScore) {
        this.id = id;
        this.username = username;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.totalScore = totalScore;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int g) { this.gamesPlayed = g; }
    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int w) { this.gamesWon = w; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int s) { this.totalScore = s; }
    
    public void addGame(boolean won, int score) {
        gamesPlayed++;
        if (won) gamesWon++;
        totalScore += score;
    }
}
