package com.example.smartchess.chess.chessboard;


import com.example.smartchess.chess.chessboard.pieces.King;
import com.example.smartchess.chess.chessboard.pieces.Piece;

import java.util.List;

public class ChessUtils {

    public static final int BOARD_SIZE = ChessGame.BOARD_SIZE;


    public static boolean isKingInCheck(Piece[][] board, Piece.Color color, Position enPassantSquare) {
        int kingRow = -1, kingCol = -1;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece instanceof King && piece.getColor() == color) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        if (kingRow == -1) {

            return false;
        }

        // Vérifier si une pièce adverse peut attaquer la case du roi
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != color) {
                    List<Position> moves = piece.getAvailableMoves(row, col, board,enPassantSquare);
                    for (Position move : moves) {
                        if (move.getRow() == kingRow && move.getCol() == kingCol) {
                            // La pièce adverse peut attaquer le roi
                            System.out.println("King is in check by " + piece.getClass().getSimpleName() + " at (" + row + ", " + col + ")");

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static Piece[][] copyBoard(Piece[][] board) {
        Piece[][] boardCopy = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    boardCopy[row][col] = piece.clone();
                } else {
                    boardCopy[row][col] = null;
                }
            }
        }
        return boardCopy;
    }


    public static boolean doesMoveResolveCheck(Piece[][] board, Piece.Color color, int fromRow, int fromCol, Position move, Position enPassantSquare) {
        Piece[][] boardCopy = copyBoard(board);
        Piece movingPiece = boardCopy[fromRow][fromCol];
        boardCopy[move.getRow()][move.getCol()] = movingPiece;
        boardCopy[fromRow][fromCol] = null;
        return !isKingInCheck(boardCopy, color, enPassantSquare);
    }

    public static boolean CanAPieceMove(Piece[][] board, Piece.Color color, Position enPassantSquare) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == color) {
                    List<Position> moves = piece.getAvailableMovesWithCheck(row, col, board,enPassantSquare);
                    if (!moves.isEmpty()) {
                        System.out.println("Cette pièce peut bouger : " + piece.getClass().getSimpleName() + " at (" + row + ", " + col + ")");
                        for (Position move : moves) {
                            System.out.println("Move: " + move.getRow() + ", " + move.getCol());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isSquareUnderAttack(Piece[][] board, int targetRow, int targetCol, Piece.Color defenderColor, Position enPassantSquare) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != defenderColor) {
                    // On utilise les mouvements disponibles (sans vérification de check) pour déterminer si la case est attaquée
                    List<Position> moves = piece.getAvailableMoves(row, col, board, enPassantSquare);
                    for (Position move : moves) {
                        if (move.getRow() == targetRow && move.getCol() == targetCol) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isPathClear(Piece[][] board, int row, int colStart, int colEnd) {
        int step = colStart < colEnd ? 1 : -1;
        // On commence après la case de départ et on s'arrête avant la case d'arrivée
        for (int col = colStart + step; col != colEnd; col += step) {
            if (board[row][col] != null) {
                return false;
            }
        }
        return true;
    }

}
