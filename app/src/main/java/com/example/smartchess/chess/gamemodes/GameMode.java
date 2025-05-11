package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public interface GameMode {

    void initGame(ChessGame game, ChessBoardView view);

    void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack);

    void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack);

    void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack);

    void onGameOver(String winner,String loser, String description);

    void beforeMovePiece(ChessGame game);

    void setDialogCallback(ChessGameController.GameOverDialogCallback callback);
}
