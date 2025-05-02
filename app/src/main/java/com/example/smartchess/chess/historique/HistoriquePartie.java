package com.example.smartchess.chess.historique;

import java.util.Date;
import java.util.List;

public class HistoriquePartie {
    private String id;
    private String player1Id;
    private String player2Id;
    private List<String> moves;
    private String winnerId;
    private Date timestamp;
    private int duration; // en secondes
    private String result; // "win", "lose", "draw"

    public HistoriquePartie() {} // Requis pour Firebase

    public HistoriquePartie(String player1Id, String player2Id, List<String> moves,
                            String winnerId, Date timestamp, int duration, String result) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.moves = moves;
        this.winnerId = winnerId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
