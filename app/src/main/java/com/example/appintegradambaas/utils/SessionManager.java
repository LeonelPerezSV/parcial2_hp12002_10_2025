package com.example.appintegradambaas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "app_session";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_REMEMBER = "remember_me";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Guardar datos de sesión
    public void saveLogin(String email, boolean remember) {
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_REMEMBER, remember);
        editor.apply();
    }

    // ✅ Método usado por SplashActivity
    public boolean isLoggedIn() {
        return prefs.contains(KEY_EMAIL);
    }

    // ✅ Método usado por ProfileActivity y HomeActivity
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Saber si se marcó "recordarme"
    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }

    // Cerrar sesión
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
