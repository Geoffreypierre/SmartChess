package com.example.smartchess.chess.controller;



import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.gamemodes.MultiplayerGameMode;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;


public class ChessGameController {

    public interface GameOverDialogCallback {
        void show(String winnerText, String eloChangeText);
    }


    private GameOverDialogCallback gameOverDialogCallback;
    private ChessGame chessGame;
    private ChessBoardView boardView;
    private GameMode gameMode;
    private PlayerInfoView playerInfoViewBlack;
    private PlayerInfoView playerInfoViewWhite;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGameController(ChessGame game, ChessBoardView view, GameMode mode, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack, GameOverDialogCallback dialogCallback) {
        this.chessGame = game;
        this.boardView = view;
        this.gameMode = mode;
        this.playerInfoViewWhite = playerInfoViewWhite;
        this.playerInfoViewBlack = playerInfoViewBlack;
        this.gameOverDialogCallback = dialogCallback;

        this.chessGame.setGameOverCallback(new ChessGame.GameOverCallback() {
            @Override
            public void onGameOver(String winner,String loser, String description) {
                gameMode.onGameOver(winner,loser, description);
            }
        });

        this.chessGame.setOnPromotionCompletedListener(finalMove -> {

            mode.onMoveValidated(finalMove, game, view, playerInfoViewWhite, playerInfoViewBlack);
        });

        this.gameMode.setDialogCallback(dialogCallback);
        this.gameMode.initGame(this.chessGame, this.boardView);
    }


    public void onCellTouched(int row, int col) {

        if (boardView.isAnimating()) {
            return;
        }

        // Première sélection : sélectionner la pièce à jouer.
        System.out.println("Cell touched: " + row + ", " + col);
        if (selectedRow == -1 && selectedCol == -1) {
            System.out.println("premiere selection");
            if (chessGame.getPiece(row, col) != null) {
                selectedRow = row;
                selectedCol = col;

            }

        } else {
            System.out.println("seconde selection");
            gameMode.beforeMovePiece(chessGame);
            System.out.println(selectedCol + " " + selectedCol + " " + row + " " + col);
            if(chessGame.getPiece(selectedRow, selectedCol) == null) {
                System.out.println("premier IF");
                selectedRow = -1;
                selectedCol = -1;
                return;
            }
            if (chessGame.getPiece(selectedRow, selectedCol).getColor() == null) {
                System.out.println("Second IF");
                selectedRow = -1;
                selectedCol = -1;
                return;
            }
            Move move = new Move(selectedRow, selectedCol, row, col,chessGame.getPiece(selectedRow,selectedCol).getColor().equals(Piece.Color.BLACK) ? "black" : "white", chessGame.getPiece(selectedRow, selectedCol).toString());
            gameMode.validateMove(move, chessGame, boardView, playerInfoViewWhite, playerInfoViewBlack);


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