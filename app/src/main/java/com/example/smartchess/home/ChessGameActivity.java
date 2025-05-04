package com.example.smartchess.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.gamemodes.LocalGameMode;
import com.example.smartchess.chess.gamemodes.MultiplayerGameMode;
import com.example.smartchess.chess.playerinfos.ChessTimer;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class ChessGameActivity extends AppCompatActivity {

    PlayerInfoView playerInfoViewWhite;
    PlayerInfoView playerInfoViewBlack;
    String playerWhiteId;
    String playerBlackId;
    ChessBoardView chessBoardView;

    ChessGame game;
    GameMode mode;

    AppCompatButton quitBtn;
    Button btnQuitLocalGame, btnDialogCancel;

    Dialog dialog;

    String multi_game_id;
    String multi_player_color;

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
        playerInfoViewWhite.setPseudo("Joueur 1");
        playerInfoViewWhite.setElo(1500);
        playerInfoViewBlack.setPseudo("Joueur 2");
        playerInfoViewBlack.setElo(1500);


        Intent intent = getIntent();
        String gameModeString = intent.getStringExtra("game_mode");
        if (gameModeString != null) {
            if (gameModeString.equals("local")) {
                mode = new LocalGameMode();

            } else if (gameModeString.equals("multiplayer")) {
                multi_game_id = intent.getStringExtra("game_id");
                multi_player_color = intent.getStringExtra("player_color");
                playerWhiteId = intent.getStringExtra("playerWhiteId");
                playerBlackId = intent.getStringExtra("playerBlackId");

                mode = new MultiplayerGameMode(multi_game_id, multi_player_color);

                if (multi_player_color.equals("white"))
                {
                    loadPlayerInfo(playerBlackId, true);
                    loadPlayerInfo(playerWhiteId, false);
                }
                else{
                    loadPlayerInfo(playerWhiteId, true);
                    loadPlayerInfo(playerBlackId, false);
                }

            }
            else {
                Toast.makeText(this, "Invalid game mode", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            mode = new LocalGameMode();
        }

        // Set up the player info views
        ChessTimer timerWhite = new ChessTimer(1*60*1000,1000);
        playerInfoViewWhite.setTimer(timerWhite);


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
                UserSession session = new UserSession(ChessGameActivity.this);
                String userId = session.getUserId();
                mode.onGameOver(null,userId,"Abandon");
                Log.e("ENDGAME", Arrays.deepToString(game.getBoard()));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mode instanceof MultiplayerGameMode) {
            UserSession session = new UserSession(this);
            String userId = session.getUserId();

            Log.w("ChessGame", "App fermée ou quittée sans fin de partie → abandon");
            mode.onGameOver(null, userId, "Abandon");
        }


    }

    @Override
    public void onBackPressed() {
        dialog.show();
    }

    private void loadPlayerInfo(String userId, boolean isWhite) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String pseudo = documentSnapshot.getString("username");
                        Long eloLong = documentSnapshot.getLong("elo");
                        String imageUrl = documentSnapshot.getString("profilePicture");
                        int elo = eloLong != null ? eloLong.intValue() : 1500;

                        if (isWhite) {
                            playerInfoViewWhite.setPseudo(pseudo);
                            playerInfoViewWhite.setElo(elo);
                            playerInfoViewWhite.setProfileImage(imageUrl);

                        } else {
                            playerInfoViewBlack.setPseudo(pseudo);
                            playerInfoViewBlack.setElo(elo);
                            playerInfoViewBlack.setProfileImage(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur chargement user " + userId, e));
    }




}