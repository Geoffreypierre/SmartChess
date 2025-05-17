package com.example.smartchess.matchmaking;

public interface MatchmakerInterface {
    void enterQueue();
    void leaveQueue();
    void endGame(String gameId);
}
