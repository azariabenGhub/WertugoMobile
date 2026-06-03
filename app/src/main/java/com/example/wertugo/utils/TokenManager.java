package com.example.wertugo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_pref";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER_ID = "USER_ID";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor; // 1. Deklarasi editor di sini

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit(); // 2. Inisialisasi editor di sini (dalam constructor)
    }

    public void saveToken(String token, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user");
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_USER_ID); // Hapus juga ID saat logout
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Simpan ID User
    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // Ambil ID User
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
}