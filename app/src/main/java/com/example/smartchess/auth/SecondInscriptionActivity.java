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
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.MainActivity;
import com.example.smartchess.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SecondInscriptionActivity extends AppCompatActivity {

    private EditText editNomUtilisateur, editAdresseMail, editMotDePasse;
    private ImageView photoProfile, togglePassword;
    private Button btnInscrire;
    private TextView btnConnexion;
    private boolean isPasswordVisible = false;
    private String niveauSelectionne;
    private Uri profileImageUri = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

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
                    saveUserToFirestore();
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

    private void saveUserToFirestore() {
        String username = editNomUtilisateur.getText().toString().trim();
        String email = editAdresseMail.getText().toString().trim();
        String password = editMotDePasse.getText().toString().trim();


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
            uploadProfileImageAndSaveUser(username, email, password, initialElo);
        } else {
            saveUserWithoutImage(username, email, password, initialElo);
        }
    }

    private void uploadProfileImageAndSaveUser(String username, String email, String password, int elo) {
        String userId = db.collection("users").document().getId();
        StorageReference imageRef = storageRef.child("profile_images/" + userId + ".jpg");

        imageRef.putFile(profileImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                User user = new User(
                                        username,
                                        email,
                                        password,
                                        elo,
                                        downloadUrl.toString()
                                );

                                saveUserToFirestore(userId, user);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Échec du téléchargement de l'image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserWithoutImage(String username, String email, String password, int elo) {
        String userId = db.collection("users").document().getId();
        User user = new User(
                username,
                email,
                password,
                elo
        );

        saveUserToFirestore(userId, user);
    }

    private void saveUserToFirestore(String userId, User user) {
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Inscription réussie !, Elo: " + user.getElo(),
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SecondInscriptionActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SecondInscriptionActivity.this,
                            "Échec de l'inscription: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}