package com.example.smartchess.chess.chessboard;

public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private String color;
    private String pieceType;

    // Champs ajoutés pour une notation complète :
    private boolean isCapture = false;
    private boolean isCheck = false;
    private boolean isCheckmate = false;
    private String promotion = null; // Pour la promotion d'un pion
    private boolean isCastling = false;

    // Obligatoire pour Firebase
    public Move() {
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, String color, String pieceType,
                boolean isCapture, boolean isCheck, boolean isCheckmate,
                String promotion, boolean isCastling) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.color = color;
        this.pieceType = pieceType;
        this.isCapture = isCapture;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;
        this.promotion = promotion;
        this.isCastling = isCastling;
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

    public boolean isCapture() {
        return isCapture;
    }

    public void setCapture(boolean capture) {
        isCapture = capture;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean isCheckmate() {
        return isCheckmate;
    }

    public void setCheckmate(boolean checkmate) {
        isCheckmate = checkmate;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public void setCastling(boolean castling) {
        isCastling = castling;
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
