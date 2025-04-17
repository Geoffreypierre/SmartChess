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
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private TextView txtUsername;
    private TextView txtEmailValue;
    private TextView txtEloValue;
    private TextView txtRankValue;
    private ImageView imgProfile;

    private UserSession userSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userSession = new UserSession(getContext());

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
     * Charge les données du profil utilisateur depuis la session
     */
    private void loadProfileData() {
        try {
            String username = userSession.getUsername();
            String email = userSession.getEmail();
            String niveau = userSession.getNiveau();
            int elo = userSession.getElo();
            String profileImageUrl = userSession.getProfileImageUrl();

            txtUsername.setText(username);
            txtEmailValue.setText(email);
            txtEloValue.setText(String.valueOf(elo));
            txtRankValue.setText(niveau);

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Picasso.get()
                        .load(profileImageUrl)
                        .placeholder(R.drawable.profile_picture_placeholder)
                        .error(R.drawable.profile_picture_placeholder)
                        .into(imgProfile);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}