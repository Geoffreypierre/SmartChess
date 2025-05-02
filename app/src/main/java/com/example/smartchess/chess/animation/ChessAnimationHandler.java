package com.example.smartchess.chess.animation;

import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;
import com.example.smartchess.chess.gamemodes.GameMode;

public class ChessAnimationHandler {

    public static void handleMoveAnimation(
            ChessBoardView boardView,

            //fonction endCallback


            int fromRow,
            int fromCol,
            int toRow,
            int toCol,
            Piece pieceToMove
    ) {
        Move move = new Move(
                fromRow, fromCol, toRow, toCol,
                pieceToMove.getColor() == Piece.Color.BLACK ? "black" : "white",
                pieceToMove.toString()
        );

        boardView.animateMove(fromRow, fromCol, toRow, toCol, pieceToMove);

        boardView.setAnimationEndCallback(() -> {



        });
    }
}
