package com.example.smartchess.services;

import com.example.smartchess.chess.historique.HistoriquePartie;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoriquePartieService {

    private final FirebaseFirestore db;

    public HistoriquePartieService() {
        db = FirebaseFirestore.getInstance();
    }

    public void ajouterPartie(HistoriquePartie partie, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("HistoriquePartie")
                .add(partie)
                .addOnSuccessListener(docRef -> {
                    String gameId = docRef.getId();
                    partie.setId(gameId);
                    lierPartieAuxJoueurs(partie, onSuccess, onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    private void lierPartieAuxJoueurs(HistoriquePartie partie, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        WriteBatch batch = db.batch();

        DocumentReference user1Ref = db.collection("users").document(partie.getPlayer1Id());
        DocumentReference user2Ref = db.collection("users").document(partie.getPlayer2Id());

        batch.update(user1Ref, "games", FieldValue.arrayUnion(partie.getId()));
        batch.update(user2Ref, "games", FieldValue.arrayUnion(partie.getId()));

        batch.commit().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void recupererPartiesUtilisateur(String userId, OnSuccessListener<List<HistoriquePartie>> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> gameIds = (List<String>) documentSnapshot.get("games");
                    if (gameIds == null || gameIds.isEmpty()) {
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<HistoriquePartie> parties = new ArrayList<>();
                    AtomicInteger count = new AtomicInteger(0);
                    for (String id : gameIds) {
                        db.collection("HistoriquePartie").document(id)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    HistoriquePartie p = doc.toObject(HistoriquePartie.class);
                                    p.setId(doc.getId());
                                    parties.add(p);
                                    if (count.incrementAndGet() == gameIds.size()) {
                                        onSuccess.onSuccess(parties);
                                    }
                                })
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
