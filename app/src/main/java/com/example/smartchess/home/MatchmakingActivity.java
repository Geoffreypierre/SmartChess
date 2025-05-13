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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MatchmakingActivity extends AppCompatActivity {

    private static final long MATCHMAKING_TIMEOUT_MS = 30_000; // 30 secondes
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    private Matchmaker matchmaker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("user_id");
        long elo = intent.getLongExtra("elo", 1200); // valeur par défaut
        String color = intent.getStringExtra("color");

        matchmaker = new Matchmaker(userId, elo, color);
        matchmaker.enterQueue();

        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");


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
                        timeoutHandler.removeCallbacks(timeoutRunnable); // on stoppe le timeout

                        String white = snapshot.child("playerWhite").getValue(String.class);
                        String black = snapshot.child("playerBlack").getValue(String.class);

                        if (userId.equals(white) || userId.equals(black)) {
                            String gameId = snapshot.getKey();

                            // Partie trouvée
                            Intent intent = new Intent(MatchmakingActivity.this, ChessGameActivity.class);
                            intent.putExtra("game_mode", "multiplayer");
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
            finish(); // ou afficher un message à l'utilisateur
        };

        timeoutHandler.postDelayed(timeoutRunnable, MATCHMAKING_TIMEOUT_MS);





    }

}