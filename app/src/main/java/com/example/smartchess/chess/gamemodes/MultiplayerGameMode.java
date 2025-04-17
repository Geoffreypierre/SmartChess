package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class MultiplayerGameMode implements GameMode {

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view) {
        // Rafraîchir la vue après un coup joué.
        System.out.println("Move validated: " + move.getRow() + ", " + move.getCol());
        view.invalidate();
    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack){
        view.rotateBoard();

        view.setSelectedCol(-1);
        view.setSelectedRow(-1);
    }
}
