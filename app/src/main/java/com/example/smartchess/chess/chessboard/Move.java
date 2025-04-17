package com.example.smartchess.chess.chessboard;

public class Move {
    private int row;
    private int col;

    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Move) {
            Move other = (Move) obj;
            return row == other.row && col == other.col;
        }
        return false;
    }
}