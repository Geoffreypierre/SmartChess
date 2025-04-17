package com.example.smartchess.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserSession {

    private SharedPreferences pref;
    private Editor editor;
    private Context context;

    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "SmartChessUserPref";

    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NIVEAU = "niveau";
    public static final String KEY_ELO = "elo";
    public static final String KEY_PROFILE_IMAGE = "profile_image";

    public UserSession(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String username, String email,
                                   String niveau, int elo, String profileImageUrl) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NIVEAU, niveau);
        editor.putInt(KEY_ELO, elo);
        editor.putString(KEY_PROFILE_IMAGE, profileImageUrl);

        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getNiveau() {
        return pref.getString(KEY_NIVEAU, "Débutant");
    }

    public int getElo() {
        return pref.getInt(KEY_ELO, 1200);
    }

    public String getProfileImageUrl() {
        return pref.getString(KEY_PROFILE_IMAGE, "");
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}