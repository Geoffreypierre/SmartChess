package com.example.smartchess.play;

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
            Toast.makeText(getContext(), "Création d'une partie différée", Toast.LENGTH_SHORT).show();
        });

        cardPartieEnLigne.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SecondPlayFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardPartieDifferee.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Création d'une partie différée", Toast.LENGTH_SHORT).show();
        });
    }
}