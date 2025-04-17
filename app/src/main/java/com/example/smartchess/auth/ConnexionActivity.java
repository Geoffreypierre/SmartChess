package com.example.smartchess.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.play.PlayActivity;
import com.example.smartchess.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ConnexionActivity extends AppCompatActivity {

    private EditText editNomUtilisateur, editMotDePasse;
    private ImageView togglePassword;
    private Button btnConnexion;
    private TextView btnInscription;
    private boolean isPasswordVisible = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connexion);

        initViews();
        setupListeners();
    }

    private void initViews() {
        editNomUtilisateur = findViewById(R.id.edit_nom_utilisateur);
        editMotDePasse = findViewById(R.id.edit_mot_de_passe);
        togglePassword = findViewById(R.id.toggle_password);
        btnConnexion = findViewById(R.id.btn_connexion);
        btnInscription = findViewById(R.id.btn_inscription);
    }

    private void setupListeners() {
        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    connectUser();
                }
            }
        });

        btnInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnexionActivity.this, InscriptionActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        String username = editNomUtilisateur.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editNomUtilisateur.setError("Veuillez entrer un nom d'utilisateur");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            editMotDePasse.setError("Veuillez entrer un mot de passe");
            isValid = false;
        }

        return isValid;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editMotDePasse.setInputType(129);  // Password hidden
            togglePassword.setImageResource(R.drawable.visible_toggle);
        } else {
            editMotDePasse.setInputType(1);  // Password visible
            togglePassword.setImageResource(R.drawable.invisible_toggle);
        }
        isPasswordVisible = !isPasswordVisible;
        editMotDePasse.setSelection(editMotDePasse.getText().length());
    }

    private void connectUser() {
        String username = editNomUtilisateur.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        showLoading(true);

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        boolean userFound = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                userFound = true;

                                String userId = document.getId();
                                String email = document.getString("email");
                                String niveau = document.getString("niveau");
                                int elo = document.getLong("elo") != null ? document.getLong("elo").intValue() : 1200;
                                String profileImageUrl = document.getString("profileImageUrl");

                                saveUserSession(userId, username, email, niveau, elo, profileImageUrl);

                                navigateToPlayScreen();
                                break;
                            }
                        }

                        if (!userFound) {
                            Toast.makeText(ConnexionActivity.this,
                                    "Nom d'utilisateur ou mot de passe incorrect",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ConnexionActivity.this,
                                "Erreur lors de la connexion: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserSession(String userId, String username, String email, String niveau, int elo, String profileImageUrl) {
        UserSession userSession = new UserSession(this);
        userSession.createLoginSession(userId, username, email, niveau, elo, profileImageUrl);
    }

    private void navigateToPlayScreen() {
        Intent intent = new Intent(ConnexionActivity.this, PlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnConnexion.setEnabled(false);
            btnConnexion.setText("Connexion en cours...");
        } else {
            btnConnexion.setEnabled(true);
            btnConnexion.setText("Se connecter");
        }
    }
}