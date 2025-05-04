package com.example.smartchess.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MatchmakingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);


        UserSession session = new UserSession(this);
        String userId = session.getUserId();

        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");

        gamesRef.orderByChild("status").equalTo("playing")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
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



    }

}