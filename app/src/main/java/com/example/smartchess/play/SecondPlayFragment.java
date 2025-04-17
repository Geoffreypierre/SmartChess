package com.example.smartchess.play;

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

public class SecondPlayFragment extends Fragment {

    private CardView cardFriendly;
    private CardView cardOnline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_online_home, container, false);

        cardFriendly = view.findViewById(R.id.card_friendly);
        cardOnline = view.findViewById(R.id.card_matchmaking);

        return view;
    }
    
}
