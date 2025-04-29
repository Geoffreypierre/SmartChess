package com.example.smartchess.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartchess.home.PlayActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("open_fragment", "profile");
        startActivity(intent);
        finish();
    }
}