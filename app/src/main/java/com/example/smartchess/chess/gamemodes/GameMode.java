package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public interface GameMode {

    void onMoveValidated(Move move, ChessGame game, ChessBoardView view);

    void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack);
}
