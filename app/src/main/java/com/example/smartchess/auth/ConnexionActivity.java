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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.play.PlayActivity;
import com.example.smartchess.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConnexionActivity extends AppCompatActivity {

    private EditText editAdresseMail, editMotDePasse;
    private ImageView togglePassword;
    private Button btnConnexion;
    private TextView btnInscription;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connexion);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        editAdresseMail = findViewById(R.id.edit_adresse_mail);
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

        String email = editAdresseMail.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editAdresseMail.setError("Veuillez entrer une adresse email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editAdresseMail.setError("Adresse email invalide");
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
        String email = editAdresseMail.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Connexion réussie
                            FirebaseUser user = mAuth.getCurrentUser();
                            loadUserInfo(user.getUid());
                        } else {
                            // Échec de la connexion
                            showLoading(false);
                            Toast.makeText(ConnexionActivity.this,
                                    "Échec de la connexion: Email ou mot de passe incorrect",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadUserInfo(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            int elo = document.getLong("elo") != null ? document.getLong("elo").intValue() : 1200;
                            String profilePicture = document.getString("profilePicture");

                            saveUserSession(userId, username, elo, profilePicture);
                            navigateToPlayScreen();
                        } else {
                            Toast.makeText(ConnexionActivity.this,
                                    "Erreur: informations utilisateur introuvables",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ConnexionActivity.this,
                                "Erreur lors du chargement des informations: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserSession(String userId, String username, int elo, String profilePicture) {
        UserSession userSession = new UserSession(this);
        userSession.createLoginSession(userId, username, elo, profilePicture);
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