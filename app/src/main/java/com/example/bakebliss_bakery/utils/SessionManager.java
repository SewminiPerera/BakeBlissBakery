package com.example.bakebliss_bakery.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {
    private static final String PREF_NAME = "BakeBlissUserSession";
    private static final String KEY_DISPLAY_NAME = "displayName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;

    public SessionManager(Context context) {
        mAuth = FirebaseAuth.getInstance();
        if (context != null) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    /** Called after a successful login to cache the display name locally */
    public void createLoginSession(String displayName) {
        if (editor != null) {
            editor.putString(KEY_DISPLAY_NAME, displayName);
            editor.apply();
        }
    }

    /** True if Firebase has a currently signed-in user */
    public boolean isLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null;
    }

    /**
     * Returns the display name of the current user.
     * Priority: Firebase displayName → cached SharedPrefs name → email prefix.
     */
    public String getUsername() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                return user.getDisplayName();
            }
            // Fall back to cached name (set during email/password register)
            if (pref != null) {
                String cached = pref.getString(KEY_DISPLAY_NAME, "");
                if (!cached.isEmpty()) return cached;
            }
            // Fall back to email prefix
            if (user.getEmail() != null) {
                return user.getEmail().split("@")[0];
            }
        }
        return "";
    }

    /** Returns the Firebase UID of the current user, or empty string */
    public String getUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : "";
    }

    /** Returns the email of the current user */
    public String getUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null && user.getEmail() != null) ? user.getEmail() : "";
    }

    /** Signs the user out of Firebase and clears cached prefs */
    public void logoutUser() {
        mAuth.signOut();
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }
}
