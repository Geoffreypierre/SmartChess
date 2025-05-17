package com.example.smartchess.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PlayFragment extends Fragment {

    private CardView cardPartieLocale;
    private CardView cardPartieEnLigne;
    private CardView cardPartieDifferee;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        cardPartieLocale = view.findViewById(R.id.card_partie_locale);
        cardPartieEnLigne = view.findViewById(R.id.card_partie_en_ligne);
        cardPartieDifferee = view.findViewById(R.id.card_partie_differee);

        setupListeners();

        return view;
    }

    private void setupListeners() {
        cardPartieLocale.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Création d'une partie locale", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), ChessGameActivity.class);
            intent.putExtra("game_mode", "local");
            startActivity(intent);


        });

        cardPartieEnLigne.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SecondPlayFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardPartieDifferee.setOnClickListener(v -> {

            UserSession session = new UserSession(getContext());
            String userId = session.getUserId();
            int elo = session.getElo();

            Intent intent = new Intent(getActivity(), MatchmakingActivity.class);

            intent.putExtra("user_id", userId);
            intent.putExtra("elo", elo);
            intent.putExtra("color", "any");
            intent.putExtra("mode","differe");

            startActivity(intent);


            // Vérifie si l'utilisateur a une partie en cours

            DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("differedGames");


            System.out.println("user id : " + userId);


            gamesRef.orderByChild("status").equalTo("playing")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String white = snapshot.child("playerWhite").getValue(String.class);
                            String black = snapshot.child("playerBlack").getValue(String.class);

                            if (userId.equals(white) || userId.equals(black)) {
                                String gameId = snapshot.getKey();

                                // PARTIE LANCEE
                                System.out.println("PARTIE TROUVEE " + gameId);

                                //quitter l'écran de matchmaking qui s'est ouvert par dessus

                                // Ferme MatchmakingActivity si elle est active
                                if (getActivity() instanceof MatchmakingActivity) {
                                    getActivity().finish();
                                }

                            }
                        }

                        @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                        @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });



        });
    }
}