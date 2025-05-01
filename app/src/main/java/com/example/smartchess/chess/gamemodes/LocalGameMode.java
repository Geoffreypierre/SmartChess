package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class LocalGameMode implements GameMode {

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());
        view.invalidate();

        onTurnChanged(game.isWhiteTurn(), game, view, playerInfoViewWhite, playerInfoViewBlack);
    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack){
        view.rotateBoard();

        game.setWhiteTurn(!game.isWhiteTurn());

        if (whiteTurn) {
            if (playerInfoViewWhite != null) playerInfoViewWhite.startTimer();
            if (playerInfoViewBlack != null) playerInfoViewBlack.pauseTimer();
        } else {
            if (playerInfoViewWhite != null) playerInfoViewWhite.pauseTimer();
            if (playerInfoViewBlack != null) playerInfoViewBlack.startTimer();
        }

        view.setSelectedCol(-1);
        view.setSelectedRow(-1);
    }

    @Override
    public void initGame(ChessGame game, ChessBoardView view) {
    }

    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        onMoveValidated(move, game, view, playerInfoViewWhite, playerInfoViewBlack);
        System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());
    }

    @Override
    public void onGameOver(String winner, String description) {
        System.out.println("Game over: " + winner + " " + description);
    }

    @Override
    public void beforeMovePiece(ChessGame game) {
    }
}