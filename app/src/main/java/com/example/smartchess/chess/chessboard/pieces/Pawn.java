package com.example.smartchess.chess.chessboard.pieces;



import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Move;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int direction = (color == Color.WHITE) ? -1 : 1;
        // Déplacement simple vers l'avant
        if (fromCol == toCol) {
            if (toRow - fromRow == direction && board[toRow][toCol] == null) {
                return true;
            }
            // Double déplacement depuis la position initiale
            if ((color == Color.WHITE && fromRow == 6) || (color == Color.BLACK && fromRow == 1)) {
                if (toRow - fromRow == 2 * direction &&
                        board[fromRow + direction][fromCol] == null &&
                        board[toRow][toCol] == null) {
                    return true;
                }
            }
        }
        // Capture en diagonale
        if (Math.abs(toCol - fromCol) == 1 && toRow - fromRow == direction) {
            if (board[toRow][toCol] != null && board[toRow][toCol].getColor() != color) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getImageResId() {
        return color == Color.WHITE ? R.drawable.pawn : R.drawable.pawn_black;
    }

    public List<Move> getAvailableMoves(int fromRow, int fromCol, Piece[][] board, Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? -1 : 1;
        int nextRow = fromRow + direction;

        // Déplacement vers l'avant si la case est libre
        if (isInBounds(nextRow, fromCol, board) && board[nextRow][fromCol] == null) {
            if (isValidMove(fromRow, fromCol, nextRow, fromCol, board)) {
                moves.add(new Move(nextRow, fromCol));
            }
            // Double déplacement depuis la ligne initiale
            int initialRow = (color == Color.WHITE) ? board.length - 2 : 1;
            if (fromRow == initialRow) {
                int twoStepsRow = fromRow + 2 * direction;
                if (isInBounds(twoStepsRow, fromCol, board) && board[twoStepsRow][fromCol] == null) {
                    if (isValidMove(fromRow, fromCol, twoStepsRow, fromCol, board)) {
                        moves.add(new Move(twoStepsRow, fromCol));
                    }
                }
            }
        }

        // Captures diagonales
        for (int dCol = -1; dCol <= 1; dCol += 2) {
            int captureCol = fromCol + dCol;
            if (isInBounds(nextRow, captureCol, board)) {
                if (board[nextRow][captureCol] != null && board[nextRow][captureCol].getColor() != color) {
                    if (isValidMove(fromRow, fromCol, nextRow, captureCol, board)) {
                        moves.add(new Move(nextRow, captureCol));
                    }
                }
                // Prise en passant
                else if (board[nextRow][captureCol] == null) {
                    // Vérifier si la case cible correspond à la case en passant enregistrée
                    if (enPassantSquare != null && enPassantSquare.equals(new Move(nextRow, captureCol))) {
                        // On crée un move en passant
                        Move enPassantMove = new Move(nextRow, captureCol);
                        moves.add(enPassantMove);
                    }
                }
            }
        }

        return moves;
    }

    public List<Move> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board,Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? -1 : 1;
        int nextRow = fromRow + direction;

        // Déplacement vers l'avant si la case est libre
        if (isInBounds(nextRow, fromCol, board) && board[nextRow][fromCol] == null) {
            if (isValidMove(fromRow, fromCol, nextRow, fromCol, board)) {
                Move moveD = new Move(nextRow, fromCol);
                if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                    moves.add(moveD);
                }
            }
            // Double déplacement depuis la ligne initiale
            int initialRow = (color == Color.WHITE) ? board.length - 2 : 1;
            if (fromRow == initialRow) {
                int twoStepsRow = fromRow + 2 * direction;
                if (isInBounds(twoStepsRow, fromCol, board) && board[twoStepsRow][fromCol] == null) {
                    if (isValidMove(fromRow, fromCol, twoStepsRow, fromCol, board)) {
                        Move moveD = new Move(twoStepsRow, fromCol);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                            moves.add(moveD);
                        }
                    }
                }
            }
        }

        // Captures diagonales
        for (int dCol = -1; dCol <= 1; dCol += 2) {
            int captureCol = fromCol + dCol;
            if (isInBounds(nextRow, captureCol, board)) {
                if (board[nextRow][captureCol] != null && board[nextRow][captureCol].getColor() != color) {
                    if (isValidMove(fromRow, fromCol, nextRow, captureCol, board)) {
                        Move moveD = new Move(nextRow, captureCol);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                            moves.add(moveD);
                        }
                    }
                }
                // Prise en passant
                else if (board[nextRow][captureCol] == null) {
                    // Vérifier si la case cible correspond à la case en passant enregistrée
                    if (enPassantSquare != null && enPassantSquare.equals(new Move(nextRow, captureCol))) {
                        // On crée un move en passant
                        Move enPassantMove = new Move(nextRow, captureCol);
                        moves.add(enPassantMove);
                    }
                }
            }
        }

        return moves;
    }


    public boolean isPromotionRow(int row, Piece[][] board) {
        // Pour les blancs, la promotion a lieu sur la première rangée (index 0)
        // Pour les noirs, sur la dernière rangée (index board.length - 1)
        System.out.println("row: " + row);
        System.out.println("board.length: " + board.length);
        System.out.println("color: " + color);
        System.out.println("isPromotionRow: " + ((color == Color.WHITE && row == 0) || (color == Color.BLACK && row == board.length - 1)));
        return (color == Color.WHITE && row == 0) || (color == Color.BLACK && row == board.length - 1);
    }

    public Piece promote(String promotionChoice) {
        // On convertit le choix en minuscule pour faciliter la comparaison.
        switch (promotionChoice.toLowerCase()) {
            case "queen":
                return new Queen(this.color);
            case "rook":
                return new Rook(this.color);
            case "bishop":
                return new Bishop(this.color);
            case "knight":
                return new Knight(this.color);
            default:
                throw new IllegalArgumentException("Promotion invalide : " + promotionChoice);
        }
    }

    private boolean isInBounds(int row, int col, Piece[][] board) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    @Override
    public Pawn clone() {
        return new Pawn(this.color);
    }
}
