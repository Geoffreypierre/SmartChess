package com.example.smartchess.chess.controller;

import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

import java.util.List;

public class ChessGameController {

    private ChessGame chessGame;
    private ChessBoardView boardView;
    private GameMode gameMode;
    private PlayerInfoView playerInfoViewBlack;
    private PlayerInfoView playerInfoViewWhite;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGameController(ChessGame game, ChessBoardView view, GameMode mode, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        this.chessGame = game;
        this.boardView = view;
        this.gameMode = mode;
        this.playerInfoViewWhite = playerInfoViewWhite;
        this.playerInfoViewBlack = playerInfoViewBlack;

        this.chessGame.setGameOverCallback(new ChessGame.GameOverCallback() {
            @Override
            public void onGameOver(String winner, String description) {
                gameMode.onGameOver(winner, description);
            }
        });

        this.gameMode.initGame(this.chessGame, this.boardView);
    }

    public void onCellTouched(int row, int col) {
        if (boardView.isAnimating()) {
            return;
        }

        System.out.println("Cell touched: " + row + ", " + col);
        if (selectedRow == -1 && selectedCol == -1) {
            Piece piece = chessGame.getPiece(row, col);
            if (piece != null) {
                boolean isWhitePiece = piece.getColor() == Piece.Color.WHITE;
                if (isWhitePiece == chessGame.isWhiteTurn()) {
                    selectedRow = row;
                    selectedCol = col;
                    boardView.setSelectedRow(row);
                    boardView.setSelectedCol(col);
                    boardView.invalidate();
                }
            }
        } else {
            Piece pieceToMove = chessGame.getPiece(selectedRow, selectedCol);

            if (pieceToMove != null) {
                Piece targetPiece = chessGame.getPiece(row, col);
                if (targetPiece != null && targetPiece.getColor() == pieceToMove.getColor()) {
                    selectedRow = row;
                    selectedCol = col;
                    boardView.setSelectedRow(row);
                    boardView.setSelectedCol(col);
                    boardView.invalidate();
                    return;
                }

                List<Position> availableMoves = pieceToMove.getAvailableMovesWithCheck(
                        selectedRow, selectedCol, chessGame.getBoard(), chessGame.getEnPassantSquare());
                Position targetPosition = new Position(row, col);

                if (availableMoves.contains(targetPosition)) {
                    gameMode.beforeMovePiece(chessGame);

                    Move move = new Move(
                            selectedRow, selectedCol, row, col,
                            pieceToMove.getColor() == Piece.Color.BLACK ? "black" : "white",
                            pieceToMove.toString()
                    );

                    final int fromRow = selectedRow;
                    final int fromCol = selectedCol;
                    final int toRow = row;
                    final int toCol = col;

                    boardView.animateMove(selectedRow, selectedCol, row, col, pieceToMove);

                    boardView.setAnimationEndCallback(new ChessBoardView.AnimationEndCallback() {
                        @Override
                        public void onAnimationEnd() {
                            boolean moveSuccessful = chessGame.movePiece(fromRow, fromCol, toRow, toCol);

                            if (moveSuccessful) {
                                gameMode.onMoveValidated(move, chessGame, boardView, playerInfoViewWhite, playerInfoViewBlack);
                            } else {
                                System.out.println("Échec du mouvement: " + fromRow + "," + fromCol + " -> " + toRow + "," + toCol);
                            }
                        }
                    });
                } else {
                    System.out.println("Mouvement invalide");
                }
            }

            selectedRow = -1;
            selectedCol = -1;
            boardView.setSelectedRow(-1);
            boardView.setSelectedCol(-1);
        }

        boardView.invalidate();
    }

    public ChessGame getChessGame() {
        return chessGame;
    }
}
