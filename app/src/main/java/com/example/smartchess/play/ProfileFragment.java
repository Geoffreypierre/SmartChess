package com.example.smartchess.play;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private TextView txtUsername;
    private TextView txtEmailValue;
    private TextView txtEloValue;
    private TextView txtRankValue;
    private ImageView imgProfile;

    private UserSession userSession;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userSession = new UserSession(getContext());
        mAuth = FirebaseAuth.getInstance();

        initViews(view);
        loadProfileData();

        return view;
    }

    private void initViews(View view) {
        txtUsername = view.findViewById(R.id.txt_username);
        txtEmailValue = view.findViewById(R.id.txt_email_value);
        txtEloValue = view.findViewById(R.id.txt_elo_value);
        txtRankValue = view.findViewById(R.id.txt_rank_value);
        imgProfile = view.findViewById(R.id.img_profile);
    }

    /**
     * Charge les données du profil utilisateur depuis la session et Firebase Auth
     */
    private void loadProfileData() {
        try {
            // Récupérer les informations stockées dans la session
            String username = userSession.getUsername();
            int elo = userSession.getElo();
            String profilePicture = userSession.getProfilePicture();

            // Récupérer l'email depuis Firebase Auth
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String email = currentUser != null ? currentUser.getEmail() : "Non disponible";

            // Déterminer le niveau en fonction de l'ELO
            String niveau = determineLevel(elo);

            // Mettre à jour l'interface
            txtUsername.setText(username);
            txtEmailValue.setText(email);
            txtEloValue.setText(String.valueOf(elo));
            txtRankValue.setText(niveau);

            if (profilePicture != null && !profilePicture.isEmpty()) {
                Picasso.get()
                        .load(profilePicture)
                        .placeholder(R.drawable.profile_picture_placeholder)
                        .error(R.drawable.profile_picture_placeholder)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.profile_picture_placeholder);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Détermine le niveau du joueur en fonction de son score ELO
     */
    private String determineLevel(int elo) {
        if (elo < 1400) {
            return "Débutant";
        } else if (elo < 1700) {
            return "Intermédiaire";
        } else {
            return "Avancé";
        }
    }
}