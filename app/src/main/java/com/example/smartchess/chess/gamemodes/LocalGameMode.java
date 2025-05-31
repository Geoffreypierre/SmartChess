package com.example.smartchess.chess.gamemodes;


import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class LocalGameMode implements GameMode {

    protected ChessGameController.GameOverDialogCallback dialogCallback;
    private boolean processingMove = false;

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        view.invalidate();
        onTurnChanged(game.isWhiteTurn(), game, view, playerInfoViewWhite, playerInfoViewBlack);
    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack){
        view.rotateBoard();

        game.setWhiteTurn(!game.isWhiteTurn());

        if (whiteTurn) {
            playerInfoViewWhite.startTimer();
            playerInfoViewBlack.pauseTimer();
            game.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
            view.setBoardOrientation(ChessGame.BoardOrientation.BLACK);


        } else {
            playerInfoViewWhite.pauseTimer();
            playerInfoViewBlack.startTimer();

            game.setBoardOrientation(ChessGame.BoardOrientation.WHITE);
            view.setBoardOrientation(ChessGame.BoardOrientation.WHITE);
        }

        view.setSelectedCol(-1);
        view.setSelectedRow(-1);
    }

    @Override
    public void initGame(ChessGame game, ChessBoardView view) {

    }

    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        if (view.isAnimating() || processingMove) {
            return;
        }

        processingMove = true;

        boolean canMove = game.canMovePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());

        if (canMove) {
            Piece pieceToMove = game.getPiece(move.getFromRow(), move.getFromCol());

            animateMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(), pieceToMove, view);

            view.setAnimationEndCallback(() -> {
                Move finalMove = game.movePiece(move);
                if (finalMove != null) {
                    onMoveValidated(move, game, view, playerInfoViewWhite, playerInfoViewBlack);
                    System.out.println("Coup validé après animation");
                } else {
                    System.out.println("Coup invalide !");
                }
                processingMove = false;
            });
        } else {
            System.out.println("Coup invalide !");
            processingMove = false;
        }
    }

    public void animateMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, ChessBoardView boardView) {
        boardView.animateMove(fromRow, fromCol, toRow, toCol, piece);
    }

    @Override
    public void onGameOver(String winner, String loser, String description) {
        if (dialogCallback != null) {
            String winnerText;
            if (winner != null) {
                winnerText = "Victoire du " + winner + " par " + description;
            } else {
                winnerText = "Match nul !";
            }
            dialogCallback.show(winnerText, "");
        }
    }

    @Override
    public void beforeMovePiece(ChessGame game) {

    }

    @Override
    public void setDialogCallback(ChessGameController.GameOverDialogCallback callback) {
        this.dialogCallback = callback;
    }
}