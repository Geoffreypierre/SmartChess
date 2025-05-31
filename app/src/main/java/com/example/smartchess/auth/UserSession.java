package com.example.smartchess.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserSession {
    private SharedPreferences pref;
    private Editor editor;
    private Context context;

    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "SmartChessUserSession";

    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ELO = "elo";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";

    public UserSession(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String username, int elo, String profilePicture) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putInt(KEY_ELO, elo);
        editor.putString(KEY_PROFILE_PICTURE, profilePicture);
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

    public int getElo() {
        return pref.getInt(KEY_ELO, 1200);
    }

    public String getProfilePicture() {
        return pref.getString(KEY_PROFILE_PICTURE, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}