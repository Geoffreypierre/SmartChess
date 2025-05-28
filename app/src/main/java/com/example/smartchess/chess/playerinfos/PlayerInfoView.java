package com.example.smartchess.chess.playerinfos;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoView extends LinearLayout {

    private ImageView profileImageView;
    private TextView pseudoTextView;
    private TextView eloTextView;
    private LinearLayout capturedPiecesLayout;
    private TextView scoreDiffTextView;

    private ChessTimer chessTimer;
    private TextView timerTextView;

    private List<Piece> capturedPieces;
    private int scoreDiff;

    public PlayerInfoView(Context context) {
        super(context);
        capturedPieces = new ArrayList<>();
        init(context);
    }

    public PlayerInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        capturedPieces = new ArrayList<>();
        init(context);
    }

    public PlayerInfoView withTimer(ChessTimer timer) {
        setTimer(timer);
        return this;
    }


    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.player_info_view, this, true);
        profileImageView = findViewById(R.id.image_profile);
        pseudoTextView = findViewById(R.id.text_pseudo);
        eloTextView = findViewById(R.id.text_elo);
        capturedPiecesLayout = findViewById(R.id.captured_pieces_layout);
        scoreDiffTextView = findViewById(R.id.text_score_diff);

        timerTextView = findViewById(R.id.text_timer);
        timerTextView.setVisibility(GONE);
    }


    public void setProfileImage(Drawable drawable) {
        profileImageView.setImageDrawable(drawable);
    }


    public void setPseudo(String pseudo) {
        pseudoTextView.setText(pseudo);
    }

    public String getPseudo() {
        return pseudoTextView.getText().toString();
    }

    public void setElo(int elo) {
        eloTextView.setText(String.valueOf(elo));
    }

    public void updateCapturedPieces(Piece piece) {
        capturedPieces.add(piece);
        ImageView pieceImageView = new ImageView(getContext());
        pieceImageView.setImageResource(piece.getImageResId());
        pieceImageView.setLayoutParams(new LayoutParams(50, 50));
        capturedPiecesLayout.addView(pieceImageView);
    }


    public void updateScoreDiff(int diff) {
        scoreDiff += diff;
        scoreDiffTextView.setText(String.valueOf(scoreDiff));
    }

    public void setTimer(ChessTimer timer) {
        this.chessTimer = timer;

        if (timerTextView != null) {
            timerTextView.setVisibility(VISIBLE);

            chessTimer.setOnTickRunnable(() -> {
                post(() -> {
                    long seconds = chessTimer.getMillisLeft() / 1000;
                    long minutes = seconds / 60;
                    long remainingSeconds = seconds % 60;
                    timerTextView.setText(String.format("%02d:%02d", minutes, remainingSeconds));
                });
            });

            chessTimer.setOnTimerFinishedListener(() -> {
                System.out.println("Timer finished");
            });
        }
    }

    public void startTimer() {
        if (chessTimer != null) chessTimer.start();
    }

    public void pauseTimer() {
        if (chessTimer != null) chessTimer.pause();
    }

    public void setProfileImage(String imageUrl) {

        Glide.with(getContext())
                .load(imageUrl)
                .placeholder(R.drawable.profile_picture_placeholder)
                .error(R.drawable.profile_picture_placeholder)
                .circleCrop()
                .into(profileImageView);
    }



}
