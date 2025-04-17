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


    private ChessGameController controller;

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

        Move enpassantSquare = chessGame.getEnPassantSquare();

        // Centrer le plateau dans la vue
        int boardLeft = (getWidth() - boardWidth) / 2;
        int boardTop = (getHeight() - boardHeight) / 2;


        // Dessin du damier et affichage des pièces
        for (int row = 0; row < ChessGame.BOARD_SIZE; row++) {
            for (int col = 0; col < ChessGame.BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 == 0;
                Paint cellPaint = isLight ? lightPaint : darkPaint;
                int left = col * cellSize;
                int top = row * cellSize;
                canvas.drawRect(left, top, left + cellSize, top + cellSize, cellPaint);

                // Surligner la case sélectionnée
                if (row == selectedRow && col == selectedCol) {
                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setStyle(Paint.Style.STROKE);
                    highlightPaint.setStrokeWidth(5);
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, highlightPaint);
                }

                // Surligner les cases disponibles si une pièce est sélectionnée
                Piece selectedPiece = chessGame.getPiece(selectedRow, selectedCol);
                if (selectedPiece != null) {
                    // Récupérer les coups possibles pour la pièce sélectionnée
                    List<Move> availableMoves = selectedPiece.getAvailableMovesWithCheck(selectedRow, selectedCol, chessGame.getBoard(),enpassantSquare);

                    // Préparer un Paint pour dessiner le surlignage (par exemple en jaune semi-transparent)
                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setAlpha(5);


                    for (Move move : availableMoves) {
                        int moveRow = move.getRow();
                        int moveCol = move.getCol();
                        int leftHighlight = boardLeft + moveCol * cellSize;
                        int topHighlight = boardTop + moveRow * cellSize;
                        // Dessiner un rectangle sur la case correspondante
                        canvas.drawRect(leftHighlight, topHighlight, leftHighlight + cellSize, topHighlight + cellSize, highlightPaint);
                    }
                }


                Piece piece = chessGame.getPiece(row, col);
                if (piece != null) {
                    int resId = piece.getImageResId();
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
                    if (drawable != null) {
                        drawable.setBounds(left, top, left + cellSize, top + cellSize);

                        //marche pas !!
                        if (piece.getColor() == Piece.Color.WHITE) {
                            //drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                        } else {
                            //drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        }

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
