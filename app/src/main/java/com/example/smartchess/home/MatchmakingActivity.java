    package com.example.smartchess.home;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.support.annotation.NonNull;
    import android.support.annotation.Nullable;
    import android.widget.Button;

    import androidx.appcompat.app.AppCompatActivity;

    import com.example.smartchess.R;
    import com.example.smartchess.auth.UserSession;
    import com.example.smartchess.matchmaking.Matchmaker;
    import com.example.smartchess.matchmaking.MatchmakerDiffere;
    import com.example.smartchess.matchmaking.MatchmakerInterface;
    import com.google.firebase.database.ChildEventListener;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    public class MatchmakingActivity extends AppCompatActivity {

        private static final long MATCHMAKING_TIMEOUT_MS = 30_000; // 30 secondes
        private Handler timeoutHandler = new Handler();
        private Runnable timeoutRunnable;

        private MatchmakerInterface matchmaker;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_matchmaking);

            Intent intent = getIntent();
            String userId = intent.getStringExtra("user_id");
            long elo = intent.getLongExtra("elo", 1200); // valeur par défaut
            String color = intent.getStringExtra("color");
            String mode = intent.getStringExtra("mode");

            final DatabaseReference gamesRef;

            if (mode.equals("multiplayer")){
                matchmaker = new Matchmaker(userId, elo, color);
                gamesRef = FirebaseDatabase.getInstance().getReference("games");
            }
            else if (mode.equals("differe")){
                matchmaker = new MatchmakerDiffere(userId, elo,color);
                gamesRef = FirebaseDatabase.getInstance().getReference("differedGames");

            }
            else {
                throw new IllegalArgumentException("Mode de jeu non valide : " + mode);
            }

            System.out.println("AVANT 11111");

            checkExistingGame(userId, gamesRef, existingGame -> {
                System.out.println("checkExistingGame : " + existingGame);
                if (existingGame != null) {
                    System.out.println("Partie trouvée 11: " + existingGame.getKey());
                    // Partie trouvée -> lancer directement l'activité de jeu
                    String white = existingGame.child("playerWhite").getValue(String.class);
                    String black = existingGame.child("playerBlack").getValue(String.class);
                    String gameId = existingGame.getKey();

                    Intent gameIntent = new Intent(MatchmakingActivity.this, ChessGameActivity.class);
                    gameIntent.putExtra("game_mode", mode);
                    gameIntent.putExtra("game_id", gameId);
                    gameIntent.putExtra("player_color", userId.equals(white) ? "white" : "black");
                    gameIntent.putExtra("playerWhiteId", white);
                    gameIntent.putExtra("playerBlackId", black);
                    startActivity(gameIntent);
                    finish();
                } else {
                    // Aucune partie -> démarrer le matchmaking
                    System.out.println("Aucune partie trouvée, démarrage du matchmaking...");
                    startMatchmaking(userId, gamesRef, mode);
                }
            });

            System.out.println("APRES 11111");




        }
        private void checkExistingGame(String userId, DatabaseReference gamesRef, OnGameFoundCallback callback) {
            gamesRef.orderByChild("status").equalTo("playing")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String white = child.child("playerWhite").getValue(String.class);
                            String black = child.child("playerBlack").getValue(String.class);

                            if (userId.equals(white) || userId.equals(black)) {
                                callback.onGameFound(child);
                                return;
                            }
                        }
                        callback.onGameFound(null); // Aucune partie trouvée
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        callback.onGameFound(null);
                    });
        }



        private interface OnGameFoundCallback {
            void onGameFound(@Nullable DataSnapshot gameSnapshot);
        }

        private void startMatchmaking(String userId, DatabaseReference gamesRef, String mode) {
            System.out.println("Démarrage du matchmaking...");
            matchmaker.enterQueue();

            Button cancelButton = findViewById(R.id.button_cancel_matchmaking);
            cancelButton.setOnClickListener(v -> {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                matchmaker.leaveQueue();
                finish();
            });

            gamesRef.orderByChild("status").equalTo("playing")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            timeoutHandler.removeCallbacks(timeoutRunnable);

                            String white = snapshot.child("playerWhite").getValue(String.class);
                            String black = snapshot.child("playerBlack").getValue(String.class);

                            if (userId.equals(white) || userId.equals(black)) {
                                String gameId = snapshot.getKey();
                                System.out.println("Partie trouvée après matchmaking !");

                                Intent intent = new Intent(MatchmakingActivity.this, ChessGameActivity.class);
                                intent.putExtra("game_mode", mode);
                                intent.putExtra("game_id", gameId);
                                intent.putExtra("player_color", userId.equals(white) ? "white" : "black");
                                intent.putExtra("playerWhiteId", white);
                                intent.putExtra("playerBlackId", black);

                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                        @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });

            timeoutRunnable = () -> {
                matchmaker.leaveQueue();
                finish(); // ou afficher un message
            };

            timeoutHandler.postDelayed(timeoutRunnable, MATCHMAKING_TIMEOUT_MS);
        }



    }

    //TODO TOUT MARCHE MAIS JUSTE CA LANCE UN NOUVEAU MATCHMAKING QUAND ON A UNE PARTIE EN COURS ET QU'ON y reTOURNE