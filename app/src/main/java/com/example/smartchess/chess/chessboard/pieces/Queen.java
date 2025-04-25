package com.example.smartchess.chess.chessboard.pieces;


import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff == colDiff || fromRow == toRow || fromCol == toCol) {

            return true;
        }
        return false;
    }

    @Override
    public int getImageResId() {
        return color == Color.WHITE ? R.drawable.queen : R.drawable.queen_black;
    }

    public List<Position> getAvailableMoves(int fromRow, int fromCol, Piece[][] board, Position enPassantSquare) {
        List<Position> moves = new ArrayList<>();
        // Combinaison des directions de la tour et du fou
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] d : directions) {
            int row = fromRow;
            int col = fromCol;
            while (true) {
                row += d[0];
                col += d[1];
                if (!isInBounds(row, col, board))
                    break;
                if (board[row][col] == null) {
                    if (isValidMove(fromRow, fromCol, row, col, board)) {
                        moves.add(new Position(row, col));
                    }
                } else {
                    if (board[row][col].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, row, col, board)) {
                            moves.add(new Position(row, col));
                        }
                    }
                    break;
                }
            }
        }
        return moves;
    }

    public List<Position> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board, Position enPassantSquare) {
        List<Position> moves = new ArrayList<>();
        // Combinaison des directions de la tour et du fou
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] d : directions) {
            int row = fromRow;
            int col = fromCol;
            while (true) {
                row += d[0];
                col += d[1];
                if (!isInBounds(row, col, board))
                    break;
                if (board[row][col] == null) {
                    if (isValidMove(fromRow, fromCol, row, col, board)) {
                        Position moveD = new Position(row, col);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                            moves.add(moveD);
                        }
                    }
                } else {
                    if (board[row][col].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, row, col, board)) {
                            Position moveD = new Position(row, col);
                            if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                                moves.add(moveD);
                            }
                        }
                    }
                    break;
                }
            }
        }
        return moves;
    }

    private boolean isInBounds(int row, int col, Piece[][] board) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    @Override
    public Queen clone() {
        return new Queen(this.color);
    }

    @Override
    public String toString() {
        return "Queen";
    }
}
