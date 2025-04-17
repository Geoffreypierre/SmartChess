package com.example.smartchess.play;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartchess.R;
import com.example.smartchess.auth.ConnexionActivity;
import com.example.smartchess.auth.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlayActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_home);

        userSession = new UserSession(this);

        if (!userSession.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        String openFragment = getIntent().getStringExtra("open_fragment");
        if (savedInstanceState == null) {
            if (openFragment != null && openFragment.equals("profile")) {
                loadFragment(new ProfileFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_profil);
            } else {
                loadFragment(new PlayFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_jouer);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_jouer) {
            fragment = new PlayFragment();
        } else if (itemId == R.id.nav_amis) {
            fragment = new FriendsFragment();
        } else if (itemId == R.id.nav_historique) {
            fragment = new HistoryFragment();
        } else if (itemId == R.id.nav_profil) {
            fragment = new ProfileFragment();
        }

        if (fragment != null) {
            return loadFragment(fragment);
        }
        return false;
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, ConnexionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}