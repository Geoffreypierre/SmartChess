package com.example.smartchess.chess.controller;

public class GameOverInfo {
    public String text;
    public String elochange;

    public GameOverInfo() {}

    public GameOverInfo(String text, String elochange) {
        this.text = text;
        this.elochange = elochange;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getElochange() {
        return elochange;
    }
    public void setElochange(String elochange) {
        this.elochange = elochange;
    }
}
