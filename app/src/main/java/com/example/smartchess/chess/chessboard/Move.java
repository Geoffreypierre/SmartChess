package com.example.smartchess.chess.chessboard;

public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private String color;
    private String pieceType;

    //Constructeur sans argument requis par Firebase
    public Move() {
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, String color, String pieceType) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.color = color;
        this.pieceType = pieceType;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPieceType() {
        return pieceType;
    }

    public void setPieceType(String pieceType) {
        this.pieceType = pieceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Move) {
            Move other = (Move) obj;
            return fromRow == other.fromRow &&
                    fromCol == other.fromCol &&
                    toRow == other.toRow &&
                    toCol == other.toCol &&
                    color.equals(other.color) &&
                    pieceType.equals(other.pieceType);
        }
        return false;
    }
}
