package com.example.smartchess.chess.controller;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class ChessGameController {

    private ChessGame chessGame;
    private ChessBoardView boardView;
    private GameMode gameMode;
    private PlayerInfoView playerInfoViewBlack;
    private PlayerInfoView playerInfoViewWhite;

    // Variables pour gérer la sélection de pièces.
    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGameController(ChessGame game, ChessBoardView view, GameMode mode, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        this.chessGame = game;
        this.boardView = view;
        this.gameMode = mode;
        this.playerInfoViewWhite = playerInfoViewWhite;
        this.playerInfoViewBlack = playerInfoViewBlack;
    }


    public void onCellTouched(int row, int col) {
        // Première sélection : sélectionner la pièce à jouer.
        System.out.println("Cell touched: " + row + ", " + col);
        if (selectedRow == -1 && selectedCol == -1) {
            if (chessGame.getPiece(row, col) != null) {
                selectedRow = row;
                selectedCol = col;

            }

        } else {
            // Deuxième sélection : tenter de déplacer la pièce sélectionnée.
            boolean moveOk = chessGame.movePiece(selectedRow, selectedCol, row, col);
            if (moveOk) {
                // Créer et transmettre le coup joué.
                System.out.println("Move made: " + selectedRow + ", " + selectedCol + " to " + row + ", " + col);
                Move move = new Move(row, col);

                // Notifier le changement de tour.
                gameMode.onTurnChanged(chessGame.isWhiteTurn(), chessGame, boardView, playerInfoViewWhite, playerInfoViewBlack);

                gameMode.onMoveValidated(move, chessGame, boardView);


            }
            // Réinitialisation de la sélection.
            selectedRow = -1;
            selectedCol = -1;
        }
        // Rafraîchir la vue
        boardView.invalidate();
    }

    public ChessGame getChessGame() {
        return chessGame;
    }
}
