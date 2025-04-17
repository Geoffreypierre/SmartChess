package com.example.smartchess.chess.chessboard.pieces;


import com.example.smartchess.chess.chessboard.Move;

import java.util.List;

public abstract class Piece {

    protected boolean hasMoved = false;

    public enum Color {
        WHITE, BLACK
    }

    protected Color color;

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }


    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board);


    public abstract int getImageResId();

    public abstract List<Move> getAvailableMoves(int fromRow, int fromCol, Piece[][] board, Move enPassantSquare);

    public abstract List<Move> getAvailableMovesWithCheck(int fromRow, int fromCol, Piece[][] board, Move enPassantSquare);

    public abstract Piece clone();

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }


}
