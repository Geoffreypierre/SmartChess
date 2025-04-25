package com.example.smartchess.chess.chessboard.pieces;


import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        // Une case dans toutes les directions
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff != 0);
    }

    @Override
    public int getImageResId() {
        // Remplacez par vos images dans res/drawable
        return color == Color.WHITE ? R.drawable.king : R.drawable.king_black;
    }

    public List<Position> getAvailableMoves(int fromRow, int fromCol, Piece[][] board, Position enPassantSquare) {
        List<Position> moves = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) continue;

                int toRow = fromRow + i;
                int toCol = fromCol + j;

                if (isInBounds(toRow, toCol, board)) {

                    if (board[toRow][toCol] == null || board[toRow][toCol].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, toRow, toCol, board)) {
                            moves.add(new Position(toRow, toCol));
                        }
                    }
                }
            }
        }

        return moves;
    }

    public List<Position> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board, Position enPassantSquare) {
        List<Position> moves = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (i == 0 && j == 0) continue;

                int toRow = fromRow + i;
                int toCol = fromCol + j;

                if (isInBounds(toRow, toCol, board)) {

                    if (board[toRow][toCol] == null || board[toRow][toCol].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, toRow, toCol, board)) {
                            Position move = new Position(toRow, toCol);
                            if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, move, enPassantSquare)) {
                                moves.add(move);
                            }
                        }
                    }

                }
            }
        }

        //Ajout du roque si le roi n'a pas encore bougé
        // On suppose ici que le roi commence en colonne 4 (pour les blancs sur la rangée 7 et pour les noirs sur la rangée 0)
        if (!this.hasMoved() && fromCol == 4) {
            // Roque côté roi (petit roque) : déplacement du roi de 2 cases vers la droite (destination colonne 6)
            if (board[fromRow][5] == null && board[fromRow][6] == null) {
                Piece potentialRook = board[fromRow][7];
                if (potentialRook != null && potentialRook instanceof Rook && !potentialRook.hasMoved()) {
                    // Vérifier que le roi et les cases intermédiaires ne sont pas attaqués
                    boolean safe = !ChessUtils.isSquareUnderAttack(board, fromRow, 4, this.getColor(), enPassantSquare)
                            && !ChessUtils.isSquareUnderAttack(board, fromRow, 5, this.getColor(), enPassantSquare)
                            && !ChessUtils.isSquareUnderAttack(board, fromRow, 6, this.getColor(), enPassantSquare);
                    if (safe) {
                        moves.add(new Position(fromRow, 6));
                    }
                }
            }
            // Roque côté dame (grand roque) : déplacement du roi de 2 cases vers la gauche (destination colonne 2)
            if (board[fromRow][3] == null && board[fromRow][2] == null && board[fromRow][1] == null) {
                Piece potentialRook = board[fromRow][0];
                if (potentialRook != null && potentialRook instanceof Rook && !potentialRook.hasMoved()) {
                    boolean safe = !ChessUtils.isSquareUnderAttack(board, fromRow, 4, this.getColor(), enPassantSquare)
                            && !ChessUtils.isSquareUnderAttack(board, fromRow, 3, this.getColor(), enPassantSquare)
                            && !ChessUtils.isSquareUnderAttack(board, fromRow, 2, this.getColor(), enPassantSquare);
                    if (safe) {
                        moves.add(new Position(fromRow, 2));
                    }
                }
            }
        }

        return moves;
    }


    private boolean isInBounds(int row, int col, Piece[][] board) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    @Override
    public King clone() {
        return new King(this.color);
    }

    @Override
    public String toString() {
        return "King";
    }

}
