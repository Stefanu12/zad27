package com.example.zad27;

import android.content.Context;
import android.content.SharedPreferences;


public class SessionPreferencesHelper {

    private static final String PREFS_NAME = "StudySessionPrefs";
    private static final String KEY_REMINDERS = "RemindersEnabled";
    private static final String KEY_SESSION_MINUTES = "SessionMinutes";

    private SharedPreferences prefs;

    public SessionPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSessionMinutes(int minutes) {
        prefs.edit().putInt(KEY_SESSION_MINUTES, minutes).apply();
    }

    public void saveRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDERS, enabled).apply();
    }

    public int loadSessionMinutes(int def) {
        return prefs.getInt(KEY_SESSION_MINUTES, def);
    }

    public boolean loadRemindersEnabled() {
        return prefs.getBoolean(KEY_REMINDERS, true);
    }
}