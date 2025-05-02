package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class LocalGameMode implements GameMode {

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        // Rafraîchir la vue après un coup joué.
        System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());
        view.invalidate();

        // Changer le tour
        onTurnChanged(game.isWhiteTurn(), game, view, playerInfoViewWhite, playerInfoViewBlack);
    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack){
        view.rotateBoard();

        game.setWhiteTurn(!game.isWhiteTurn());

        if (whiteTurn) {
            playerInfoViewWhite.startTimer();
            playerInfoViewBlack.pauseTimer();
        } else {
            playerInfoViewWhite.pauseTimer();
            playerInfoViewBlack.startTimer();
        }

        view.setSelectedCol(-1);
        view.setSelectedRow(-1);
    }

    @Override
    public void initGame(ChessGame game, ChessBoardView view) {

    }

    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        boolean moveOk = game.movePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
        if (moveOk) {
            onMoveValidated(move, game, view, playerInfoViewWhite, playerInfoViewBlack);
            System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());

        } else {
            System.out.println("Coup invalide !");
        }
    }

    @Override
    public void onGameOver(String winner, String description) {

    }

    @Override
    public void beforeMovePiece(ChessGame game) {

    }

}