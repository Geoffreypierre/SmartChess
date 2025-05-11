package com.example.smartchess.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.example.smartchess.services.chat.ChatAdapter;
import com.example.smartchess.services.chat.ChatMessage;
import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.gamemodes.GameMode;
import com.example.smartchess.chess.gamemodes.LocalGameMode;
import com.example.smartchess.chess.gamemodes.MultiplayerGameMode;
import com.example.smartchess.chess.playerinfos.ChessTimer;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    AppCompatButton chatBtn;
    Button btnQuitLocalGame, btnDialogCancel;

    Dialog quitDialog;
    Dialog chatDialog;

    String multi_game_id;
    String multi_player_color;

    private DatabaseReference chatRef;
    private ChatAdapter chatAdapter;
    private String currentUserName;
    private String currentUserId;
    private ChildEventListener chatListener;

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
        chatBtn = findViewById(R.id.chat_button);

        UserSession session = new UserSession(this);
        currentUserId = session.getUserId();

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
                chatBtn.setVisibility(View.GONE);

            } else if (gameModeString.equals("multiplayer")) {
                multi_game_id = intent.getStringExtra("game_id");
                multi_player_color = intent.getStringExtra("player_color");
                playerWhiteId = intent.getStringExtra("playerWhiteId");
                playerBlackId = intent.getStringExtra("playerBlackId");

                mode = new MultiplayerGameMode(multi_game_id, multi_player_color);

                if (multi_player_color.equals("white")) {
                    loadPlayerInfo(playerBlackId, true);
                    loadPlayerInfo(playerWhiteId, false);
                } else {
                    loadPlayerInfo(playerWhiteId, true);
                    loadPlayerInfo(playerBlackId, false);
                }

                chatRef = FirebaseDatabase.getInstance().getReference("chats").child(multi_game_id);
                setupChatListener();
            }
            else {
                Toast.makeText(this, "Invalid game mode", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            mode = new LocalGameMode();
            chatBtn.setVisibility(View.GONE); // Hide chat button in local mode
        }

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
                playerInfoViewWhite, playerInfoViewBlack, this::showGameOverDialogMulti);
        chessBoardView.setGameController(controller);

        setupQuitDialog();
        setupChatDialog();

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitDialog.show();
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatDialog.show();
            }
        });
    }

    private void setupQuitDialog() {
        quitDialog = new Dialog(ChessGameActivity.this);
        quitDialog.setContentView(R.layout.custom_dialog_quit_local_game);
        quitDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quitDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        quitDialog.setCancelable(false);

        btnQuitLocalGame = quitDialog.findViewById(R.id.btn_quitter_local_game);
        btnDialogCancel = quitDialog.findViewById(R.id.btn_annuler_local_game);

        btnQuitLocalGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChessGameActivity.this, "Fin de la partie", Toast.LENGTH_SHORT).show();
                quitDialog.dismiss();
                String userId = currentUserId;
                mode.onGameOver(null, userId, "Abandon");

                if (mode instanceof MultiplayerGameMode && chatRef != null) {
                    chatRef.removeValue();
                }

                Log.e("ENDGAME", Arrays.deepToString(game.getBoard()));

            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitDialog.dismiss();
            }
        });
    }

    private void setupChatDialog() {
        chatDialog = new Dialog(ChessGameActivity.this);
        chatDialog.setContentView(R.layout.chat_dialog);
        chatDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chatDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));

        RecyclerView recyclerView = chatDialog.findViewById(R.id.recyclerViewChat);
        EditText editTextMessage = chatDialog.findViewById(R.id.editTextMessage);
        ImageButton buttonSend = chatDialog.findViewById(R.id.buttonSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this);
        recyclerView.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty() && chatRef != null) {


                    ChatMessage chatMessage = new ChatMessage(
                            currentUserId,
                            playerInfoViewBlack.getPseudo(),
                            message,
                            System.currentTimeMillis()
                    );
                    chatRef.push().setValue(chatMessage);

                    editTextMessage.setText("");
                }
            }
        });
    }

    private void setupChatListener() {
        if (chatRef != null) {
            chatListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                    if (message != null && chatAdapter != null) {
                        System.out.println("Message reçu : " + message.getMessage());
                        System.out.println("Sender : " + message.getSenderName());
                        chatAdapter.addMessage(message);

                        RecyclerView recyclerView = chatDialog.findViewById(R.id.recyclerViewChat);
                        if (recyclerView != null) {
                            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ChatError", "Database error: " + databaseError.getMessage());
                }
            };

            chatRef.addChildEventListener(chatListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mode instanceof MultiplayerGameMode) {
            if (chatRef != null && chatListener != null) {
                chatRef.removeEventListener(chatListener);
            }

            Log.w("ChessGame", "App fermée ou quittée sans fin de partie → abandon");
            mode.onGameOver(null, currentUserId, "Abandon");

            if (chatRef != null) {
                chatRef.removeValue();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        quitDialog.show();
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

    private void showGameOverDialogMulti(String winnerText, String eloChangeText) {
        Dialog gameOverDialog = new Dialog(this);
        gameOverDialog.setContentView(R.layout.dialog_game_over);
        gameOverDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gameOverDialog.setCancelable(false);

        TextView textWinner = gameOverDialog.findViewById(R.id.textWinner);
        TextView textElo = gameOverDialog.findViewById(R.id.textEloChange);
        Button btnQuit = gameOverDialog.findViewById(R.id.btn_quit_game);

        textWinner.setText(winnerText);
        textElo.setText(eloChangeText);

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverDialog.dismiss();
                finish(); // ou retourner au menu principal
            }
        });

        gameOverDialog.show();
    }

}