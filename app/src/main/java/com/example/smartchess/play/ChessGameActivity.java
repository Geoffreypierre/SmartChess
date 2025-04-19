package com.example.smartchess.play;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.smartchess.R;
import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.gamemodes.LocalGameMode;
import com.example.smartchess.chess.gamemodes.MultiplayerGameMode;
import com.example.smartchess.chess.playerinfos.ChessTimer;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;

public class ChessGameActivity extends AppCompatActivity {

    PlayerInfoView playerInfoViewWhite;
    PlayerInfoView playerInfoViewBlack;

    ChessBoardView chessBoardView;

    ChessGame game;
    GameMode mode;

    AppCompatButton quitBtn;
    Button btnQuitLocalGame, btnDialogCancel;

    Dialog dialog;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chess_game);

        playerInfoViewWhite = findViewById(R.id.playerInfoWhite);
        playerInfoViewBlack = findViewById(R.id.playerInfoBlack);

        chessBoardView = findViewById(R.id.chessBoardView);

        quitBtn = findViewById(R.id.quit_button);

        game = new ChessGame();

        Intent intent = getIntent();
        String gameModeString = intent.getStringExtra("game_mode");
        if (gameModeString != null) {
            if (gameModeString.equals("local")) {
                mode = new LocalGameMode();
            } else {
                mode = new MultiplayerGameMode();
            }
        } else {
            mode = new LocalGameMode();
        }

        // Set up the player info views
        playerInfoViewWhite.setPseudo("Player 1");
        playerInfoViewWhite.setElo(1500);
        ChessTimer timerWhite = new ChessTimer(1*60*1000,1000);
        playerInfoViewWhite.setTimer(timerWhite);

        playerInfoViewBlack.setPseudo("Player 2");
        playerInfoViewBlack.setElo(2700);
        ChessTimer timerBlack = new ChessTimer(1*60*1000,1000);
        playerInfoViewBlack.setTimer(timerBlack);


        chessBoardView.setOnPieceCapturedListener(new ChessGame.OnPieceCapturedListener() {
            @Override
            public void onPieceCaptured(Piece capturedPiece, boolean capturedByWhite) {

                if (capturedByWhite) {
                    playerInfoViewWhite.updateCapturedPieces(capturedPiece);
                    playerInfoViewWhite.updateScoreDiff(1);
                } else {
                    playerInfoViewBlack.updateCapturedPieces(capturedPiece);
                    playerInfoViewBlack.updateScoreDiff(1);
                }

            }
        });

        ChessGameController controller = new ChessGameController(game, chessBoardView, mode,
                playerInfoViewWhite, playerInfoViewBlack);
        chessBoardView.setGameController(controller);

        dialog = new Dialog(ChessGameActivity.this);
        dialog.setContentView(R.layout.custom_dialog_quit_local_game);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        dialog.setCancelable(false);

        btnQuitLocalGame = dialog.findViewById(R.id.btn_quitter_local_game);
        btnDialogCancel = dialog.findViewById(R.id.btn_annuler_local_game);

        btnQuitLocalGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChessGameActivity.this, "Fin de la partie", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });


    }
}