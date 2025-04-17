package com.example.smartchess.chess.chessboard.pieces;


import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessUtils;
import com.example.smartchess.chess.chessboard.Move;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(Color color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        // se déplace en L
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public int getImageResId() {
        return color == Color.WHITE ? R.drawable.knight : R.drawable.knight_black;
    }

    public List<Move> getAvailableMoves(int fromRow, int fromCol, Piece[][] board,Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
        };
        for (int[] move : knightMoves) {
            int toRow = fromRow + move[0];
            int toCol = fromCol + move[1];
            if (isInBounds(toRow, toCol, board)) {
                if (board[toRow][toCol] == null || board[toRow][toCol].getColor() != this.getColor()) {
                    if (isValidMove(fromRow, fromCol, toRow, toCol, board)) {
                        moves.add(new Move(toRow, toCol));
                    }
                }
            }
        }
        return moves;
    }

    public List<Move> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board,Move enPassantSquare) {
        List<Move> moves = new ArrayList<>();
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
        };
        for (int[] move : knightMoves) {
            int toRow = fromRow + move[0];
            int toCol = fromCol + move[1];
            if (isInBounds(toRow, toCol, board)) {
                if (board[toRow][toCol] == null || board[toRow][toCol].getColor() != this.getColor()) {
                    if (isValidMove(fromRow, fromCol, toRow, toCol, board)) {
                        Move moveD = new Move(toRow, toCol);
                        if (ChessUtils.doesMoveResolveCheck(board, this.getColor(), fromRow, fromCol, moveD, enPassantSquare)) {
                            moves.add(moveD);
                        }
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
    public Knight clone() {
        return new Knight(this.color);
    }
}
