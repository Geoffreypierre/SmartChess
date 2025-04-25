package com.example.smartchess.chess.chessboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;


import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;

import java.util.List;

public class ChessBoardView extends View {

    private ChessGame chessGame;
    private Paint lightPaint;
    private Paint darkPaint;
    private int cellSize;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private ChessGame.BoardOrientation boardOrientation = ChessGame.BoardOrientation.WHITE;

    private ChessGameController controller;

    public void setBoardOrientation(ChessGame.BoardOrientation orientation) {
        this.boardOrientation = orientation;
        invalidate(); // redessine le plateau
    }

    public void setGameController(ChessGameController controller) {
        this.controller = controller;
        this.chessGame = controller.getChessGame();
    }



    public ChessBoardView(Context context) {
        super(context);
        init();
    }

    public ChessBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        chessGame = new ChessGame();

        // Définition des couleurs des cases
        lightPaint = new Paint();
        lightPaint.setColor(Color.parseColor("#EEEED2"));
        darkPaint = new Paint();
        darkPaint.setColor(Color.parseColor("#769656"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        cellSize = size / ChessGame.BOARD_SIZE;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int boardCols = chessGame.getBoard()[0].length;
        int boardRows = chessGame.getBoard().length;
        int boardWidth = cellSize * boardCols;
        int boardHeight = cellSize * boardRows;

        Position enpassantSquare = chessGame.getEnPassantSquare();

        // Centrer le plateau dans la vue
        int boardLeft = (getWidth() - boardWidth) / 2;
        int boardTop = (getHeight() - boardHeight) / 2;


        // Dessin du damier et affichage des pièces
        for (int row = 0; row < ChessGame.BOARD_SIZE; row++) {
            for (int col = 0; col < ChessGame.BOARD_SIZE; col++) {

                // Appliquer l'orientation du plateau
                int drawRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? row : 7 - row;
                int drawCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? col : 7 - col;

                boolean isLight = (drawRow + drawCol) % 2 == 0;
                Paint cellPaint = isLight ? lightPaint : darkPaint;
                int left = drawCol * cellSize;
                int top = drawRow * cellSize;
                canvas.drawRect(left, top, left + cellSize, top + cellSize, cellPaint);

                // Surlignage sélection
                if (row == selectedRow && col == selectedCol) {
                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setStyle(Paint.Style.STROKE);
                    highlightPaint.setStrokeWidth(5);
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, highlightPaint);
                }

                // Coup possible
                Piece selectedPiece = chessGame.getPiece(selectedRow, selectedCol);
                if (selectedPiece != null) {
                    List<Position> availableMoves = selectedPiece.getAvailableMovesWithCheck(
                            selectedRow, selectedCol, chessGame.getBoard(), chessGame.getEnPassantSquare()
                    );

                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setAlpha(5);

                    for (Position move : availableMoves) {
                        int moveRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? move.getRow() : 7 - move.getRow();
                        int moveCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? move.getCol() : 7 - move.getCol();
                        int leftHighlight = moveCol * cellSize;
                        int topHighlight = moveRow * cellSize;
                        canvas.drawRect(leftHighlight, topHighlight, leftHighlight + cellSize, topHighlight + cellSize, highlightPaint);
                    }
                }

                // Affichage de la pièce
                Piece piece = chessGame.getPiece(row, col);
                if (piece != null) {
                    int resId = piece.getImageResId();
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
                    if (drawable != null) {
                        drawable.setBounds(left, top, left + cellSize, top + cellSize);
                        drawable.draw(canvas);
                    }
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            if (boardOrientation == ChessGame.BoardOrientation.BLACK) {
                row = 7 - row;
                col = 7 - col;
            }

            if (controller != null) {
                setSelectedCol(col);
                setSelectedRow(row);
                controller.onCellTouched(row, col);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }


    public void setOnPieceCapturedListener(ChessGame.OnPieceCapturedListener listener) {
        chessGame.setOnPieceCapturedListener(listener);
    }

    public void rotateBoard() {
        System.out.println("Rotation du plateau pour le jeu local");
        // Inverser les lignes et colonnes pour faire pivoter le plateau


        invalidate();
    }

    public void showTurnIndicator(boolean whiteTurn) {
        // Affichez une indication du tour (exemple simple via un log ou un dessin dans la vue)
        System.out.println("Tour: " + (whiteTurn ? "Blancs" : "Noirs"));
    }

    public void setSelectedRow(int row) {
        this.selectedRow = row;
    }
    public void setSelectedCol(int col) {
        this.selectedCol = col;
    }
}
