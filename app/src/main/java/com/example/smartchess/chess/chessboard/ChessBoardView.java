package com.example.smartchess.chess.chessboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

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

    private boolean isAnimating = false;
    private int fromRow = -1;
    private int fromCol = -1;
    private int toRow = -1;
    private int toCol = -1;
    private float animProgress = 0f;
    private Piece animatedPiece = null;
    public static final int ANIMATION_DURATION = 300;

    private AnimationEndCallback animationEndCallback;

    public interface AnimationEndCallback {
        void onAnimationEnd();
    }

    public void setBoardOrientation(ChessGame.BoardOrientation orientation) {
        this.boardOrientation = orientation;
        invalidate();
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

        int boardLeft = (getWidth() - boardWidth) / 2;
        int boardTop = (getHeight() - boardHeight) / 2;

        for (int row = 0; row < ChessGame.BOARD_SIZE; row++) {
            for (int col = 0; col < ChessGame.BOARD_SIZE; col++) {
                int drawRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? row : 7 - row;
                int drawCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? col : 7 - col;

                boolean isLight = (drawRow + drawCol) % 2 == 0;
                Paint cellPaint = isLight ? lightPaint : darkPaint;
                int left = drawCol * cellSize;
                int top = drawRow * cellSize;
                canvas.drawRect(left, top, left + cellSize, top + cellSize, cellPaint);

                if (row == selectedRow && col == selectedCol) {
                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setStyle(Paint.Style.STROKE);
                    highlightPaint.setStrokeWidth(5);
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, highlightPaint);
                }

                Piece selectedPiece = chessGame.getPiece(selectedRow, selectedCol);
                if (selectedPiece != null) {
                    List<Position> availableMoves = selectedPiece.getAvailableMovesWithCheck(
                            selectedRow, selectedCol, chessGame.getBoard(), chessGame.getEnPassantSquare());

                    Paint highlightPaint = new Paint();
                    highlightPaint.setColor(Color.YELLOW);
                    highlightPaint.setAlpha(100);

                    for (Position move : availableMoves) {
                        int moveRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? move.getRow() : 7 - move.getRow();
                        int moveCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? move.getCol() : 7 - move.getCol();
                        int leftHighlight = moveCol * cellSize;
                        int topHighlight = moveRow * cellSize;
                        canvas.drawRect(leftHighlight, topHighlight, leftHighlight + cellSize, topHighlight + cellSize, highlightPaint);
                    }
                }

                if (isAnimating && (row == fromRow && col == fromCol)) continue;
                if (isAnimating && (row == toRow && col == toCol)) continue;

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

        if (isAnimating && animatedPiece != null) {
            int fromDrawRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? fromRow : 7 - fromRow;
            int fromDrawCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? fromCol : 7 - fromCol;
            int toDrawRow = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? toRow : 7 - toRow;
            int toDrawCol = (boardOrientation == ChessGame.BoardOrientation.WHITE) ? toCol : 7 - toCol;

            float currentX = fromDrawCol * cellSize + (toDrawCol - fromDrawCol) * cellSize * animProgress;
            float currentY = fromDrawRow * cellSize + (toDrawRow - fromDrawRow) * cellSize * animProgress;

            int resId = animatedPiece.getImageResId();
            Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
            if (drawable != null) {
                drawable.setBounds((int) currentX, (int) currentY, (int) currentX + cellSize, (int) currentY + cellSize);
                drawable.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimating) {
            return true;
        }

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

    public void animateMove(final int fromRow, final int fromCol, final int toRow, final int toCol, final Piece piece) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.animatedPiece = piece;
        this.isAnimating = true;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                animatedPiece = null;
                invalidate();

                if (animationEndCallback != null) {
                    animationEndCallback.onAnimationEnd();
                    animationEndCallback = null;
                }
            }
        });

        animator.start();
    }

    public void setAnimationEndCallback(AnimationEndCallback callback) {
        this.animationEndCallback = callback;
    }

    public void setOnPieceCapturedListener(ChessGame.OnPieceCapturedListener listener) {
        chessGame.setOnPieceCapturedListener(listener);
    }

    public void rotateBoard() {
        System.out.println("Rotation du plateau pour le jeu local");
        invalidate();
    }

    public void showTurnIndicator(boolean whiteTurn) {
        System.out.println("Tour: " + (whiteTurn ? "Blancs" : "Noirs"));
    }

    public void setSelectedRow(int row) {
        this.selectedRow = row;
    }

    public void setSelectedCol(int col) {
        this.selectedCol = col;
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}
