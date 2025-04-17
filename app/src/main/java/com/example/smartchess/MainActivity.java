package com.example.smartchess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.auth.ConnexionActivity;
import com.example.smartchess.auth.InscriptionActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button btnInscription = findViewById(R.id.btnInscription);
        Button btnConnexion = findViewById(R.id.btnConnexion);

        btnInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InscriptionActivity.class);
                startActivity(intent);
            }
        });

        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConnexionActivity.class);
                startActivity(intent);
            }
        });
    }
}