package com.example.smartchess.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.R;

public class InscriptionActivity extends AppCompatActivity {

    private RelativeLayout optionDebutant, optionIntermediaire, optionAvance;
    private ImageView imgDebutant, imgIntermediaire, imgAvance;
    private Button btnValider;
    private String niveauSelectionne = "Débutant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inscription);

        optionDebutant = findViewById(R.id.option_debutant);
        optionIntermediaire = findViewById(R.id.option_intermediaire);
        optionAvance = findViewById(R.id.option_avance);

        imgDebutant = findViewById(R.id.img_debutant);
        imgIntermediaire = findViewById(R.id.img_intermediaire);
        imgAvance = findViewById(R.id.img_avance);

        btnValider = findViewById(R.id.btnValider);

        optionDebutant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionnerOption("Débutant");
            }
        });

        optionIntermediaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionnerOption("Intermédiaire");
            }
        });

        optionAvance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionnerOption("Avancé");
            }
        });

        btnValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InscriptionActivity.this, SecondInscriptionActivity.class);
                intent.putExtra("NIVEAU_SELECTIONNE", niveauSelectionne);
                startActivity(intent);
            }
        });

        selectionnerOption("Débutant");
    }

    private void selectionnerOption(String niveau) {
        niveauSelectionne = niveau;

        optionDebutant.setBackgroundResource(R.drawable.insc_unselected_option_background);
        optionIntermediaire.setBackgroundResource(R.drawable.insc_unselected_option_background);
        optionAvance.setBackgroundResource(R.drawable.insc_unselected_option_background);

        imgDebutant.setImageResource(R.drawable.insc_pawn);
        imgIntermediaire.setImageResource(R.drawable.insc_knight);
        imgAvance.setImageResource(R.drawable.insc_rook);

        switch (niveau) {
            case "Débutant":
                optionDebutant.setBackgroundResource(R.drawable.insc_selected_option_background);
                imgDebutant.setImageResource(R.drawable.insc_pawn_focus);
                break;
            case "Intermédiaire":
                optionIntermediaire.setBackgroundResource(R.drawable.insc_selected_option_background);
                imgIntermediaire.setImageResource(R.drawable.insc_knight_focus);
                break;
            case "Avancé":
                optionAvance.setBackgroundResource(R.drawable.insc_selected_option_background);
                imgAvance.setImageResource(R.drawable.insc_rook_focus);
                break;
        }
    }
}