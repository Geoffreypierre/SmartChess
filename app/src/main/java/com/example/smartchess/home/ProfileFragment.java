package com.example.smartchess.home;

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
    private ImageView imgTrophy;

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
        imgTrophy = view.findViewById(R.id.img_trophy);
    }

    private void loadProfileData() {
        try {
            String username = userSession.getUsername();
            int elo = userSession.getElo();
            String profilePicture = userSession.getProfilePicture();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            String email = currentUser != null ? currentUser.getEmail() : "Non disponible";

            RankInfo rankInfo = determineRank(elo);

            txtUsername.setText(username);
            txtEmailValue.setText(email);
            txtEloValue.setText(String.valueOf(elo));
            txtRankValue.setText(rankInfo.rankName);
            imgTrophy.setImageResource(rankInfo.rankIcon);

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
     * - Bronze : 0-1199
     * - Argent : 1200-1499
     * - Or : 1500-1799
     * - Platine : 1800-2099
     * - Diamant : 2100+
     */
    private RankInfo determineRank(int elo) {
        if (elo < 1200) {
            return new RankInfo("Bronze", R.drawable.rank_bronze);
        } else if (elo < 1500) {
            return new RankInfo("Argent", R.drawable.rank_silver);
        } else if (elo < 1800) {
            return new RankInfo("Or", R.drawable.rank_gold);
        } else if (elo < 2100) {
            return new RankInfo("Platine", R.drawable.rank_platine);
        } else {
            return new RankInfo("Diamant", R.drawable.rank_diamond);
        }
    }

    private static class RankInfo {
        public final String rankName;
        public final int rankIcon;

        public RankInfo(String rankName, int rankIcon) {
            this.rankName = rankName;
            this.rankIcon = rankIcon;
        }
    }
}