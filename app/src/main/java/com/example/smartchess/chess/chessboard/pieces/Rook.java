package com.example.smartchess.chess.chessboard.pieces;



import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Move;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        //horizontalement ou verticalement
        if (fromRow == toRow || fromCol == toCol) {

            return true;
        }
        return false;
    }

    @Override
    public int getImageResId() {
        return color == Color.WHITE ? R.drawable.rook : R.drawable.rook_black;
    }

    public List<Move> getAvailableMoves(int fromRow, int fromCol, Piece[][] board,Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
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
                        moves.add(new Move(row, col));
                    }
                } else {
                    if (board[row][col].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, row, col, board)) {
                            moves.add(new Move(row, col));
                        }
                    }
                    break;
                }
            }
        }
        return moves;
    }

    public List<Move> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board,Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
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
                        Move moveD = new Move(row, col);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                            moves.add(moveD);
                        }
                    }
                } else {
                    if (board[row][col].getColor() != this.getColor()) {
                        if (isValidMove(fromRow, fromCol, row, col, board)) {
                            Move moveD = new Move(row, col);
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
    public Rook clone() {
        return new Rook(this.color);
    }


}
