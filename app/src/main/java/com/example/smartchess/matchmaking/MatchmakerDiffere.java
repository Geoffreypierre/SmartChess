package com.example.smartchess.matchmaking;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MatchmakerDiffere implements MatchmakerInterface {
    private final DatabaseReference waitingRef;
    private final DatabaseReference gamesRef;
    private final String myUid;
    private final long myElo;
    private final String myColor; // "white", "black" ou "any"
    private String gameId;

    public MatchmakerDiffere(String uid, long elo, String color) {
        this.myUid =uid;
        this.myElo = elo;
        this.myColor = color;
        this.waitingRef = FirebaseDatabase.getInstance().getReference("matchmakingDiffere/waiting");
        this.gamesRef = FirebaseDatabase.getInstance().getReference("differedGames");
    }

    public void enterQueue() {

        System.out.println("ENTER QUEUE");
        Map<String, Object> data = new HashMap<>();
        data.put("elo", myElo);
        data.put("color", myColor);
        data.put("timestamp", System.currentTimeMillis());
        System.out.println("TEST22");


        waitingRef.child(myUid).setValue(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Écriture dans la file OK !");
                        findOpponent();
                    } else {
                        System.out.println("Échec écriture !");
                        task.getException().printStackTrace();
                    }
                });
    }

    private void findOpponent() {
        System.out.println("FIND OPPONENT");

        waitingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String otherUid = child.getKey();
                    if (otherUid == null || otherUid.equals(myUid)) continue;

                    Long otherElo = child.child("elo").getValue(Long.class);
                    String otherColorRaw = child.child("color").getValue(String.class);

                    if (otherElo == null || otherColorRaw == null) continue;

                    String myColorNorm = myColor == null ? "" : myColor.trim().toLowerCase();
                    String otherColorNorm = otherColorRaw.trim().toLowerCase();

                    System.out.println("Comparing with user: " + otherUid);
                    System.out.println("  My ELO: " + myElo + " / Opponent ELO: " + otherElo);
                    System.out.println("  My Color: [" + myColorNorm + "] / Opponent Color: [" + otherColorNorm + "]");

                    if (Math.abs(otherElo - myElo) <= 100) {
                        boolean compatible =
                                ("any".equals(myColorNorm) && !"any".equals(otherColorNorm)) ||
                                        (!"any".equals(myColorNorm) && "any".equals(otherColorNorm)) ||
                                        ("white".equals(myColorNorm) && "black".equals(otherColorNorm)) ||
                                        ("black".equals(myColorNorm) && "white".equals(otherColorNorm)) ||
                                        ("any".equals(myColorNorm) && "any".equals(otherColorNorm));

                        System.out.println("  Compatible? " + compatible);

                        if (compatible) {
                            System.out.println("✅ Match trouvé entre " + myUid + " et " + otherUid);

                            String whiteUid;
                            String blackUid;

                            if ("white".equals(myColorNorm) || "black".equals(otherColorNorm)) {
                                whiteUid = myUid;
                                blackUid = otherUid;
                            } else if ("white".equals(otherColorNorm) || "black".equals(myColorNorm)) {
                                whiteUid = otherUid;
                                blackUid = myUid;
                            } else {
                                // Les deux ont any
                                if (Math.random() < 0.5) {
                                    whiteUid = myUid;
                                    blackUid = otherUid;
                                } else {
                                    whiteUid = otherUid;
                                    blackUid = myUid;
                                }
                            }


                            createGame(whiteUid, blackUid);

                            waitingRef.child(myUid).removeValue();
                            waitingRef.child(otherUid).removeValue();
                            return;
                        }
                    }
                }
                System.out.println("❌ Aucun adversaire compatible trouvé.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Matchmaker", "Erreur de recherche", error.toException());
            }
        });
    }


    private void createGame(String whiteUid, String blackUid) {
        DatabaseReference newGameRef = gamesRef.push();

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("playerWhite", whiteUid);
        gameData.put("playerBlack", blackUid);
        gameData.put("turn", "white");
        gameData.put("status", "playing");
        gameData.put("createdAt", System.currentTimeMillis());
        gameData.put("moves", new HashMap<>());

        newGameRef.setValue(gameData)
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Partie créée !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur création partie", e));

        gameId = newGameRef.getKey();
    }

    public void leaveQueue() {
        waitingRef.child(myUid).removeValue();
    }

    //terminer la partie en la supprimant
    public void endGame(String gameId) {
        gamesRef.child(gameId).removeValue()
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Partie terminée !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors de la suppression de la partie", e));
    }

}
