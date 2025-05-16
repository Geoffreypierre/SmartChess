package com.example.smartchess.home;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartchess.R;

import java.util.List;

public class MoveHistoryAdapter extends RecyclerView.Adapter<MoveHistoryAdapter.MoveViewHolder> {
    private List<String> moves;

    public MoveHistoryAdapter(List<String> moves) {
        this.moves = moves;
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_move, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        int moveNumber = position + 1;
        holder.txtMoveNumber.setText(moveNumber + ".");

        int whiteIndex = position * 2;
        int blackIndex = whiteIndex + 1;

        if (whiteIndex < moves.size()) {
            String whiteMove = moves.get(whiteIndex);
            holder.txtWhiteMove.setText(getPieceNotation(whiteMove));
            setPieceImage(holder.imgWhitePiece, whiteMove);
        } else {
            holder.txtWhiteMove.setText("O_O");
            holder.imgWhitePiece.setVisibility(View.INVISIBLE);
        }

        if (blackIndex < moves.size()) {
            String blackMove = moves.get(blackIndex);
            holder.txtBlackMove.setText(getPieceNotation(blackMove));
            setPieceImage(holder.imgBlackPiece, blackMove);
        } else {
            holder.txtBlackMove.setText("O_O");
            holder.imgBlackPiece.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (moves.size() + 1) / 2;
    }

    private String getPieceNotation(String move) {
        if (move == null || move.isEmpty()) return "O_O";

        String notation = move.replaceAll("[+#]", "");

        if (notation.equals("O-O") || notation.equals("O-O-O")) {
            return notation;
        }

        if (notation.length() >= 2) {
            return notation.substring(Math.max(0, notation.length() - 2));
        }

        return notation;
    }

    private void setPieceImage(ImageView imageView, String move) {
        if (move == null || move.isEmpty()) {
            imageView.setVisibility(View.INVISIBLE);
            return;
        }

        char firstChar = move.charAt(0);
        int drawableResId = R.drawable.pawn;

        switch (firstChar) {
            case 'P':
            case 'p':
                drawableResId = R.drawable.pawn;
                break;
            case 'N':
            case 'n':
                drawableResId = R.drawable.knight_white;
                break;
            case 'B':
            case 'b':
                drawableResId = R.drawable.bishop_white;
                break;
            case 'R':
            case 'r':
                drawableResId = R.drawable.rook_white;
                break;
            case 'Q':
            case 'q':
                drawableResId = R.drawable.queen_white;
                break;
            case 'K':
            case 'k':
                drawableResId = R.drawable.king;
                break;
            default:
                if (firstChar >= 'a' && firstChar <= 'h') {
                    drawableResId = R.drawable.pawn;
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                    return;
                }
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(drawableResId);

        imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    class MoveViewHolder extends RecyclerView.ViewHolder {
        TextView txtMoveNumber, txtWhiteMove, txtBlackMove;
        ImageView imgWhitePiece, imgBlackPiece;

        MoveViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMoveNumber = itemView.findViewById(R.id.txt_move_number);
            txtWhiteMove = itemView.findViewById(R.id.txt_white_move);
            txtBlackMove = itemView.findViewById(R.id.txt_black_move);
            imgWhitePiece = itemView.findViewById(R.id.img_white_piece);
            imgBlackPiece = itemView.findViewById(R.id.img_black_piece);
        }
    }
}