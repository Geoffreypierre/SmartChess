package com.example.smartchess.chess.chessboard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.pieces.Piece;

public class PromotionDialogHandler implements ChessGame.OnPromotionNeededListener {

    private Context context;
    private String selectedPromotion = "queen";
    private ChessBoardView chessBoardView;

    public PromotionDialogHandler(Context context) {
        this.context = context;
    }

    public void setChessBoardView(ChessBoardView chessBoardView) {
        this.chessBoardView = chessBoardView;
    }

    @Override
    public void onPromotionNeeded(int row, int col, Piece.Color pieceColor, final ChessGame.PromotionCallback callback) {
        showPromotionDialog(pieceColor, new PromotionSelectionCallback() {
            @Override
            public void onPromotionSelected(String promotionType) {
                callback.onPromotionSelected(promotionType);

                if (chessBoardView != null) {
                    chessBoardView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chessBoardView.invalidate();
                        }
                    }, 50);
                }
            }
        });
    }

    public interface PromotionSelectionCallback {
        void onPromotionSelected(String promotionType);
    }

    private void showPromotionDialog(Piece.Color pieceColor, final PromotionSelectionCallback callback) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_promotion);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageButton btnRook = dialog.findViewById(R.id.btn_promotion_tour);
        ImageButton btnKnight = dialog.findViewById(R.id.btn_promotion_cavalier);
        ImageButton btnBishop = dialog.findViewById(R.id.btn_promotion_fou);
        ImageButton btnQueen = dialog.findViewById(R.id.btn_promotion_dame);
        Button btnValidate = dialog.findViewById(R.id.btn_valider_promotion);

        if (pieceColor == Piece.Color.BLACK) {
            btnRook.setImageResource(R.drawable.rook_black);
            btnKnight.setImageResource(R.drawable.knight_black);
            btnBishop.setImageResource(R.drawable.bishop_black);
            btnQueen.setImageResource(R.drawable.queen_black);
        }

        View.OnClickListener pieceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonSelections(btnRook, btnKnight, btnBishop, btnQueen);

                view.setBackgroundResource(R.drawable.promotion_selected_piece_background);

                if (view.getId() == R.id.btn_promotion_tour) {
                    selectedPromotion = "rook";
                } else if (view.getId() == R.id.btn_promotion_cavalier) {
                    selectedPromotion = "knight";
                } else if (view.getId() == R.id.btn_promotion_fou) {
                    selectedPromotion = "bishop";
                } else if (view.getId() == R.id.btn_promotion_dame) {
                    selectedPromotion = "queen";
                }
            }
        };

        btnRook.setOnClickListener(pieceClickListener);
        btnKnight.setOnClickListener(pieceClickListener);
        btnBishop.setOnClickListener(pieceClickListener);
        btnQueen.setOnClickListener(pieceClickListener);

        btnQueen.performClick();

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (callback != null) {
                    callback.onPromotionSelected(selectedPromotion);
                }
            }
        });

        dialog.show();
    }

    private void resetButtonSelections(ImageButton... buttons) {
        for (ImageButton button : buttons) {
            button.setBackgroundResource(android.R.color.transparent);
        }
    }
}