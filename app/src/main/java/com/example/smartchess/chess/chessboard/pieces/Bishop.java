package com.example.smartchess.chess.chessboard.pieces;


import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        // En diagonale
        if (rowDiff == colDiff) {

            return true;
        }
        return false;
    }

    @Override
    public int getImageResId() {
        return color == Color.WHITE ? R.drawable.bishop : R.drawable.bishop_black;
    }


    public List<Position> getAvailableMoves(int fromRow, int fromCol, Piece[][] board, Position enPassantSquare) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = { {-1, -1}, {-1, 1}, {1, -1}, {1, 1} };
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
        int[][] directions = { {-1, -1}, {-1, 1}, {1, -1}, {1, 1} };
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
                        Position move = new Position(row, col);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, move, enPassantSquare)) {
                            moves.add(move);
                        }
                    }
                } else {
                    if (board[row][col].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, row, col, board)) {
                            Position move = new Position(row, col);
                            if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, move , enPassantSquare)) {
                                moves.add(move);
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
    public Bishop clone() {
        return new Bishop(this.getColor());
    }

    @Override
    public String toString() {
        return "Bishop";
    }

}
