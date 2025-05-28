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
import com.example.smartchess.chess.gamemodes.DiffereGameMode;
import com.example.smartchess.services.chat.ChatAdapter;
import com.example.smartchess.services.chat.ChatMessage;
import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.PromotionDialogHandler;
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
    AppCompatButton backBtn;

    ChessGame game;
    GameMode mode;
    PromotionDialogHandler promotionDialogHandler;

    AppCompatButton quitBtn;
    AppCompatButton chatBtn;

    Dialog quitDialog;
    Dialog chatDialog;

    String multi_game_id;
    String multi_player_color;
    String title;

    private DatabaseReference chatRef;
    private ChatAdapter chatAdapter;
    private String currentUserName;
    private String currentUserId;
    private TextView titleView;
    private TextView quitDialogText;
    private ChildEventListener chatListener;

    Dialog quitDialogLocal;
    Dialog quitDialogMultiplayer;
    Dialog quitDialogDiffere;

    Button btnQuitLocalGame, btnDialogCancelLocal;
    Button btnAbandonMultiplayer, btnCancelMultiplayer;
    Button btnQuitDiffere, btnCancelDiffere;

    private boolean isActivityDestroyed = false;

    @SuppressLint({"UseCompatLoadingForDrawables", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isActivityDestroyed) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chess_game);

        playerInfoViewWhite = findViewById(R.id.playerInfoWhite);
        playerInfoViewBlack = findViewById(R.id.playerInfoBlack);
        titleView = findViewById(R.id.title);
        backBtn = findViewById(R.id.back_button);

        chessBoardView = findViewById(R.id.chessBoardView);

        quitBtn = findViewById(R.id.quit_button);
        chatBtn = findViewById(R.id.chat_button);
        quitDialogText = findViewById(R.id.quit_dialog_text);

        UserSession session = new UserSession(this);
        currentUserId = session.getUserId();
        currentUserName = session.getUsername();

        game = new ChessGame();
        playerInfoViewWhite.setPseudo("Joueur 1");
        playerInfoViewWhite.setElo(1500);
        playerInfoViewBlack.setPseudo("Joueur 2");
        playerInfoViewBlack.setElo(1500);

        promotionDialogHandler = new PromotionDialogHandler(this);
        promotionDialogHandler.setChessBoardView(chessBoardView);
        game.setOnPromotionNeededListener(promotionDialogHandler);

        Intent intent = getIntent();
        String gameModeString = intent.getStringExtra("game_mode");
        if (gameModeString != null) {
            if (gameModeString.equals("local")) {
                titleView.setText("Partie locale");
                mode = new LocalGameMode();
                chatBtn.setVisibility(View.GONE);
                backBtn.setVisibility(View.GONE);

            } else if (gameModeString.equals("multiplayer")) {
                titleView.setText("Partie en ligne");
                multi_game_id = intent.getStringExtra("game_id");
                multi_player_color = intent.getStringExtra("player_color");
                playerWhiteId = intent.getStringExtra("playerWhiteId");
                playerBlackId = intent.getStringExtra("playerBlackId");

                mode = new MultiplayerGameMode(multi_game_id, multi_player_color);

                backBtn.setVisibility(View.GONE);

                if (multi_player_color.equals("white")) {
                    loadPlayerInfo(playerBlackId, true);
                    loadPlayerInfo(playerWhiteId, false);
                } else {
                    loadPlayerInfo(playerWhiteId, true);
                    loadPlayerInfo(playerBlackId, false);
                }

                ((MultiplayerGameMode) mode).startListeningForAbandon(currentUserId);

                chatRef = FirebaseDatabase.getInstance().getReference("chats").child(multi_game_id);
                setupChatListener();
                setupChatDialog();
            }
            else if (gameModeString.equals("differe")) {
                titleView.setText("Partie en différé");
                multi_game_id = intent.getStringExtra("game_id");
                multi_player_color = intent.getStringExtra("player_color");
                playerWhiteId = intent.getStringExtra("playerWhiteId");
                playerBlackId = intent.getStringExtra("playerBlackId");

                mode = new DiffereGameMode(multi_game_id, multi_player_color);

                backBtn.setVisibility(View.VISIBLE);

                if (multi_player_color.equals("white")) {
                    loadPlayerInfo(playerBlackId, true);
                    loadPlayerInfo(playerWhiteId, false);
                } else {
                    loadPlayerInfo(playerWhiteId, true);
                    loadPlayerInfo(playerBlackId, false);
                }

                chatRef = FirebaseDatabase.getInstance().getReference("chats").child(multi_game_id);
                setupChatListener();
                setupChatDialog();
            }
            else {
                Toast.makeText(this, "Invalid game mode", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            mode = new LocalGameMode();
            chatBtn.setVisibility(View.GONE);
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

        ChessGameController controller;
        if (mode instanceof LocalGameMode) {
            controller = new ChessGameController(game, chessBoardView, mode,
                    playerInfoViewWhite, playerInfoViewBlack, this::showGameOverDialogLocal);
        } else {
            controller = new ChessGameController(game, chessBoardView, mode,
                    playerInfoViewWhite, playerInfoViewBlack, this::showGameOverDialogMulti);
        }
        chessBoardView.setGameController(controller);

        setupQuitDialogs();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    finishActivity();
                }
            }
        });

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    showAppropriateQuitDialog();
                }
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed && chatDialog != null) {
                    chatDialog.show();
                }
            }
        });
    }

    private void setupChatDialog() {
        chatDialog = new Dialog(this);
        chatDialog.setContentView(R.layout.chat_dialog);

        chatDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
        );
        chatDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        chatDialog.setCancelable(true);

        if (chatDialog.getWindow() != null) {
            chatDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }

        RecyclerView recyclerViewChat = chatDialog.findViewById(R.id.recyclerViewChat);
        chatAdapter = new ChatAdapter(this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        EditText editTextMessage = chatDialog.findViewById(R.id.editTextMessage);
        ImageButton buttonSend = chatDialog.findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString().trim();
                if (!messageText.isEmpty() && chatRef != null) {
                    ChatMessage message = new ChatMessage(
                            currentUserId,
                            currentUserName != null ? currentUserName : "Player",
                            messageText,
                            System.currentTimeMillis()
                    );

                    chatRef.push().setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                editTextMessage.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChessGameActivity.this,
                                        "Erreur envoi message", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
    }

    private void setupQuitDialogs() {
        quitDialogLocal = new Dialog(ChessGameActivity.this);
        quitDialogLocal.setContentView(R.layout.custom_dialog_quit_local_game);
        quitDialogLocal.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quitDialogLocal.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        quitDialogLocal.setCancelable(false);

        btnQuitLocalGame = quitDialogLocal.findViewById(R.id.btn_quitter_local_game);
        btnDialogCancelLocal = quitDialogLocal.findViewById(R.id.btn_annuler_local_game);

        btnQuitLocalGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    Toast.makeText(ChessGameActivity.this, "Fin de la partie", Toast.LENGTH_SHORT).show();
                    quitDialogLocal.dismiss();
                    finishActivity();
                }
            }
        });

        btnDialogCancelLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    quitDialogLocal.dismiss();
                }
            }
        });

        quitDialogMultiplayer = new Dialog(ChessGameActivity.this);
        quitDialogMultiplayer.setContentView(R.layout.dialog_quit_multiplayer);
        quitDialogMultiplayer.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quitDialogMultiplayer.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        quitDialogMultiplayer.setCancelable(false);

        btnAbandonMultiplayer = quitDialogMultiplayer.findViewById(R.id.btn_abandon_multiplayer);
        btnCancelMultiplayer = quitDialogMultiplayer.findViewById(R.id.btn_cancel_multiplayer);

        btnAbandonMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    Toast.makeText(ChessGameActivity.this, "Vous avez abandonné la partie", Toast.LENGTH_SHORT).show();
                    quitDialogMultiplayer.dismiss();

                    String winner = game.isWhiteTurn() ? "Noirs" : "Blancs";
                    mode.onGameOver(winner, currentUserId, "Abandon");

                    if (chatRef != null) {
                        chatRef.removeValue();
                    }

                    finishActivity();
                }
            }
        });

        btnCancelMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    quitDialogMultiplayer.dismiss();
                }
            }
        });

        quitDialogDiffere = new Dialog(ChessGameActivity.this);
        quitDialogDiffere.setContentView(R.layout.dialog_quit_differe);
        quitDialogDiffere.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quitDialogDiffere.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_bg_dialog));
        quitDialogDiffere.setCancelable(false);

        btnQuitDiffere = quitDialogDiffere.findViewById(R.id.btn_quit_differe);
        btnCancelDiffere = quitDialogDiffere.findViewById(R.id.btn_cancel_differe);

        btnQuitDiffere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    Toast.makeText(ChessGameActivity.this, "Partie terminée", Toast.LENGTH_SHORT).show();
                    quitDialogDiffere.dismiss();

                    String winner = game.isWhiteTurn() ? "Noirs" : "Blancs";
                    mode.onGameOver(winner, currentUserId, "Abandon");

                    finishActivity();
                }
            }
        });

        btnCancelDiffere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    quitDialogDiffere.dismiss();
                }
            }
        });
    }

    private void setupChatListener() {
        if (chatRef != null) {
            chatListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    if (!isActivityDestroyed) {
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
        isActivityDestroyed = true;

        if (mode instanceof MultiplayerGameMode) {
            if (chatRef != null && chatListener != null) {
                chatRef.removeEventListener(chatListener);
            }

            if (!(mode instanceof DiffereGameMode)) {
                System.out.println("Abandon de la partie, fermeture app");
                mode.onGameOver(null, currentUserId, "Abandon");
            }

            if (chatRef != null && !(mode instanceof DiffereGameMode)) {
                chatRef.removeValue();
            }
        }

        if (quitDialogLocal != null && quitDialogLocal.isShowing()) {
            quitDialogLocal.dismiss();
        }
        if (quitDialogMultiplayer != null && quitDialogMultiplayer.isShowing()) {
            quitDialogMultiplayer.dismiss();
        }
        if (quitDialogDiffere != null && quitDialogDiffere.isShowing()) {
            quitDialogDiffere.dismiss();
        }
        if (chatDialog != null && chatDialog.isShowing()) {
            chatDialog.dismiss();
        }
    }

    private void showAppropriateQuitDialog() {
        if (mode instanceof LocalGameMode) {
            if (quitDialogLocal != null) {
                quitDialogLocal.show();
            }
        } else if (mode instanceof DiffereGameMode) {
            if (quitDialogDiffere != null) {
                quitDialogDiffere.show();
            }
        } else if (mode instanceof MultiplayerGameMode) {
            if (quitDialogMultiplayer != null) {
                quitDialogMultiplayer.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mode instanceof DiffereGameMode) {
            if (!isActivityDestroyed) {
                finishActivity();
            }
        } else {
            if (!isActivityDestroyed) {
                showAppropriateQuitDialog();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void loadPlayerInfo(String userId, boolean isWhite) {
        if (!isActivityDestroyed) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!isActivityDestroyed && documentSnapshot.exists()) {
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
                    .addOnFailureListener(e -> {
                        if (!isActivityDestroyed) {
                            Log.e("Firestore", "Erreur chargement user " + userId, e);
                        }
                    });
        }
    }

    private void showGameOverDialogLocal(String winnerText, String eloChangeText) {
        if (isActivityDestroyed) return;

        Dialog gameOverDialog = new Dialog(this);
        gameOverDialog.setContentView(R.layout.dialog_game_over);

        gameOverDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.75),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        gameOverDialog.setCancelable(false);

        TextView textWinner = gameOverDialog.findViewById(R.id.textWinner);
        TextView textElo = gameOverDialog.findViewById(R.id.textEloChange);
        Button btnQuit = gameOverDialog.findViewById(R.id.btn_quit_game);

        String localWinnerText = formatLocalWinnerText(winnerText);
        textWinner.setText(localWinnerText);

        textElo.setVisibility(View.GONE);

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    gameOverDialog.dismiss();
                    finishActivity();
                }
            }
        });

        gameOverDialog.show();
    }

    private void showGameOverDialogMulti(String winnerText, String eloChangeText) {
        if (isActivityDestroyed) return;

        Dialog gameOverDialog = new Dialog(this);
        gameOverDialog.setContentView(R.layout.dialog_game_over);

        gameOverDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.75),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        gameOverDialog.setCancelable(false);

        TextView textWinner = gameOverDialog.findViewById(R.id.textWinner);
        TextView textElo = gameOverDialog.findViewById(R.id.textEloChange);
        Button btnQuit = gameOverDialog.findViewById(R.id.btn_quit_game);

        textWinner.setText(winnerText);
        textElo.setText(eloChangeText);

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActivityDestroyed) {
                    gameOverDialog.dismiss();
                    finishActivity();
                }
            }
        });

        gameOverDialog.show();
    }

    private String formatLocalWinnerText(String winnerText) {
        if (winnerText == null) {
            return "Match nul !";
        }

        if (winnerText.toLowerCase().contains("white")) {
            return winnerText.replaceAll("(?i)white", "Joueur 1");
        }
        else if (winnerText.toLowerCase().contains("black")) {
            return winnerText.replaceAll("(?i)black", "Joueur 2");
        }
        else {
            return winnerText;
        }
    }

    private void finishActivity() {
        isActivityDestroyed = true;
        finish();
    }
}