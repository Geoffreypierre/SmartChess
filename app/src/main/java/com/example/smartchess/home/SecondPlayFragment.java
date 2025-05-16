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

public class SecondPlayFragment extends Fragment {

    private CardView cardFriendly;
    private CardView cardOnline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_online_home, container, false);

        cardFriendly = view.findViewById(R.id.card_friendly);
        cardOnline = view.findViewById(R.id.card_matchmaking);

        System.out.println("AVANT TOUT");

        setupListeners();







        return view;
    }


    private void setupListeners() {

        cardFriendly.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Matchmaking amical", Toast.LENGTH_SHORT).show();

            /*
            System.out.println("AVANT 11111");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            System.out.println("AVANT 222222");
            DatabaseReference myRef = database.getReference();

            myRef.setValue("Hello, World!").addOnSuccessListener(aVoid -> {
                        System.out.println("APRES 666666");
                    })
                    .addOnFailureListener(e -> {
                        System.out.println("APRES 777777");
                        // Handle failure
                        System.out.println(e.getMessage());
                    });;
            System.out.println("AVANT 444444");

             */
        });


        cardOnline.setOnClickListener(v -> {

            Toast.makeText(getContext(), "Matchmaking en ligne", Toast.LENGTH_SHORT).show();
            UserSession session = new UserSession(getContext());
            String userId = session.getUserId();
            int elo = session.getElo();

            Intent intent = new Intent(getActivity(), MatchmakingActivity.class);

            intent.putExtra("user_id", userId);
            intent.putExtra("elo", elo);
            intent.putExtra("color", "any");

            startActivity(intent);


            // Vérifie si l'utilisateur a une partie en cours

            DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");


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
