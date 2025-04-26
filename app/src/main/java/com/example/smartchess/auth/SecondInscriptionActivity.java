package com.example.smartchess.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.MainActivity;
import com.example.smartchess.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecondInscriptionActivity extends AppCompatActivity {

    private EditText editNomUtilisateur, editAdresseMail, editMotDePasse;
    private ImageView photoProfile, togglePassword;
    private Button btnInscrire;
    private TextView btnConnexion;
    private LinearProgressIndicator progressIndicator;
    private boolean isPasswordVisible = false;
    private String niveauSelectionne;
    private Uri profileImageUri = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    photoProfile.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inscription_second);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (getIntent().hasExtra("NIVEAU_SELECTIONNE")) {
            niveauSelectionne = getIntent().getStringExtra("NIVEAU_SELECTIONNE");
        } else {
            niveauSelectionne = "Débutant";
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        editNomUtilisateur = findViewById(R.id.edit_nom_utilisateur);
        editAdresseMail = findViewById(R.id.edit_adresse_mail);
        editMotDePasse = findViewById(R.id.edit_mot_de_passe);
        photoProfile = findViewById(R.id.photo_profile);
        togglePassword = findViewById(R.id.toggle_password);
        btnInscrire = findViewById(R.id.btn_inscrire);
        btnConnexion = findViewById(R.id.btn_connexion);
        progressIndicator = findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.GONE);
    }

    private void setupListeners() {
        photoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });

        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnInscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    registerUser();
                }
            }
        });

        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondInscriptionActivity.this, ConnexionActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        String username = editNomUtilisateur.getText().toString().trim();
        String email = editAdresseMail.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        if (username.isEmpty()) {
            editNomUtilisateur.setError("Veuillez entrer un nom d'utilisateur");
            isValid = false;
        }

        if (email.isEmpty()) {
            editAdresseMail.setError("Veuillez entrer une adresse mail");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editAdresseMail.setError("Adresse mail invalide");
            isValid = false;
        }

        if (password.isEmpty()) {
            editMotDePasse.setError("Veuillez entrer un mot de passe");
            isValid = false;
        } else if (password.length() < 6) {
            editMotDePasse.setError("Le mot de passe doit contenir au moins 6 caractères");
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

    private void registerUser() {
        String email = editAdresseMail.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserInfo(user.getUid());
                        } else {
                            Toast.makeText(SecondInscriptionActivity.this,
                                    "Échec de l'inscription: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserInfo(String userId) {
        String username = editNomUtilisateur.getText().toString().trim();

        int initialElo;
        switch (niveauSelectionne) {
            case "Intermédiaire":
                initialElo = 1500;
                break;
            case "Avancé":
                initialElo = 1800;
                break;
            default:
                initialElo = 1200;
                break;
        }

        if (profileImageUri != null) {
            uploadProfileImageAndSaveUser(userId, username, initialElo);
        } else {
            saveUserToFirestore(userId, username, initialElo, "");
        }
    }

    private void uploadProfileImageAndSaveUser(String userId, String username, int elo) {
        // Simplifier la référence de stockage pour éviter des problèmes de chemin
        StorageReference profileImagesRef = storage.getReference("profile_images");

        // Générer un nom de fichier unique basé sur l'ID utilisateur
        StorageReference imageRef = profileImagesRef.child(userId + ".jpg");

        progressIndicator.setVisibility(View.VISIBLE);
        btnInscrire.setEnabled(false);

        imageRef.putFile(profileImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressIndicator.setVisibility(View.GONE);
                        btnInscrire.setEnabled(true);

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                saveUserToFirestore(userId, username, elo, downloadUrl.toString());
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(SecondInscriptionActivity.this,
                                    "Impossible d'obtenir l'URL de l'image: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            saveUserToFirestore(userId, username, elo, "");
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnInscrire.setEnabled(true);

                    // Message d'erreur plus détaillé pour le débogage
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Échec du téléchargement de l'image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Même en cas d'échec, on enregistre l'utilisateur sans image
                    saveUserToFirestore(userId, username, elo, "");
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressIndicator.setProgress((int) progress);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, int elo, String profilePicture) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", username);
        user.put("elo", elo);
        user.put("profilePicture", profilePicture);
        user.put("friends", new ArrayList<String>());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Inscription réussie !, Elo: " + elo,
                            Toast.LENGTH_LONG).show();

                    // Sauvegarder les infos utilisateur dans UserSession
                    UserSession userSession = new UserSession(SecondInscriptionActivity.this);
                    userSession.createLoginSession(userId, username, elo, profilePicture);

                    Intent intent = new Intent(SecondInscriptionActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Échec de l'enregistrement des informations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}