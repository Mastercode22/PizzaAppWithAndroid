package com.delaroystudios.pizza.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.delaroystudios.pizza.models.User;

public class SessionManager {
    private static final String PREF_NAME = "pizza_app_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";

    // NEW: Key for Dark Mode preference
    private static final String KEY_IS_DARK_MODE = "is_dark_mode_enabled";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.apply();
    }

    public void updateUserProfile(User user) {
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.apply();
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;

        User user = new User();
        user.setUserId(pref.getInt(KEY_USER_ID, -1));
        user.setUsername(pref.getString(KEY_USERNAME, ""));
        user.setFullName(pref.getString(KEY_FULL_NAME, ""));
        user.setEmail(pref.getString(KEY_EMAIL, ""));
        user.setPhone(pref.getString(KEY_PHONE, ""));
        user.setAddress(pref.getString(KEY_ADDRESS, ""));
        return user;
    }

    public String getUserName() {
        // Returns the full name if available, otherwise returns username
        String fullName = pref.getString(KEY_FULL_NAME, "");
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return pref.getString(KEY_USERNAME, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public int getCurrentUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    // ===================================
    // NEW DARK MODE FUNCTIONS
    // ===================================

    /**
     * Saves the user's preference for Dark Mode.
     * @param isEnabled true for Dark Mode, false for Light Mode.
     */
    public void setDarkModeEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_IS_DARK_MODE, isEnabled);
        editor.apply();
    }

    /**
     * Retrieves the user's Dark Mode preference.
     * Default value is false (Light Mode).
     * @return true if Dark Mode is enabled, false otherwise.
     */
    public boolean isDarkModeEnabled() {
        return pref.getBoolean(KEY_IS_DARK_MODE, false);
    }
}