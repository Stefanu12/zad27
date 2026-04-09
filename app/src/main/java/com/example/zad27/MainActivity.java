package com.example.zad27;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_SESSION_MINUTES = 45;
    private static final String KEY_HISTORY = "SessionHistory";
    private static final String KEY_ADAPTIVE_BREAK = "AdaptiveBreak";

    private Switch switchReminders;
    private Switch switchAdaptiveBreak;
    private SeekBar seekBarSession;
    private TextView tvSessionValue;
    private TextView tvSummary;

    private SessionPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchReminders = findViewById(R.id.switchReminders);
        switchAdaptiveBreak = findViewById(R.id.switchAdaptiveBreak);
        seekBarSession = findViewById(R.id.seekBarSession);
        tvSessionValue = findViewById(R.id.tvSessionValue);
        tvSummary = findViewById(R.id.tvSummary);

        prefsHelper = new SessionPreferencesHelper(this);

        loadSettings();
        setupListeners();
    }

    private void setupListeners() {

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.saveRemindersEnabled(isChecked);
            updateSummary();
        });

        switchAdaptiveBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("StudySessionPrefs", MODE_PRIVATE)
                    .edit().putBoolean(KEY_ADAPTIVE_BREAK, isChecked).apply();
            updateSummary();
        });

        seekBarSession.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minutes = progressToMinutes(progress);
                updateSessionLabel(minutes);
                updateSummary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int min = progressToMinutes(seekBar.getProgress());

                prefsHelper.saveSessionMinutes(min);

                // zadanie 1
                if (min > 70 && !switchReminders.isChecked()) {
                    Toast.makeText(MainActivity.this,
                            "Długie sesje wymagają przypomnień",
                            Toast.LENGTH_SHORT).show();

                    switchReminders.setChecked(true);
                    prefsHelper.saveRemindersEnabled(true);
                }

                updateHistory(min);
            }
        });
    }

    private void loadSettings() {
        boolean reminders = prefsHelper.loadRemindersEnabled();
        int minutes = prefsHelper.loadSessionMinutes(DEFAULT_SESSION_MINUTES);

        boolean adaptive = getSharedPreferences("StudySessionPrefs", MODE_PRIVATE)
                .getBoolean(KEY_ADAPTIVE_BREAK, false);

        switchReminders.setChecked(reminders);
        switchAdaptiveBreak.setChecked(adaptive);
        seekBarSession.setProgress(minutesToProgress(minutes));

        updateSessionLabel(minutes);
        updateSummary();
    }

    private int progressToMinutes(int progress) {
        return 15 + (progress * 5);
    }

    private int minutesToProgress(int minutes) {
        int clamped = Math.max(15, Math.min(90, minutes));
        return (clamped - 15) / 5;
    }

    private int calculateFocusPoints(int minutes, boolean reminders) {
        int base = minutes / 5;
        return reminders ? base + 2 : base;
    }

    private int calculateBreak(int minutes) {
        return (int) Math.ceil(minutes * 0.2);
    }

    private void updateSessionLabel(int minutes) {
        tvSessionValue.setText(String.format(Locale.getDefault(), "%d min", minutes));
    }

    private void updateSummary() {
        boolean reminders = switchReminders.isChecked();
        boolean adaptive = switchAdaptiveBreak.isChecked();
        int minutes = progressToMinutes(seekBarSession.getProgress());

        int points = calculateFocusPoints(minutes, reminders);
        int avg = calculateAverage();

        String reminderText = reminders ? "włączone" : "wyłączone";

        String breakText = "";
        if (adaptive) {
            breakText = "\n• Przerwa: " + calculateBreak(minutes) + " min";
        }

        String summary = "Plan sesji:\n"
                + "• Czas: " + minutes + " min\n"
                + "• Przypomnienia: " + reminderText + "\n"
                + "• Punkty: " + points + "\n"
                + "• Średnia: " + avg + " min"
                + breakText;

        tvSummary.setText(summary);
        tvSummary.setTextColor(getLoadColor(minutes));
    }

    private int getLoadColor(int minutes) {
        if (minutes <= 35) return Color.GREEN;
        if (minutes <= 60) return Color.rgb(255, 160, 0);
        return Color.RED;
    }


    private void updateHistory(int newValue) {
        String history = getSharedPreferences("StudySessionPrefs", MODE_PRIVATE)
                .getString(KEY_HISTORY, "");

        String[] parts = history.isEmpty() ? new String[]{} : history.split(",");

        StringBuilder newHistory = new StringBuilder();

        for (int i = Math.max(0, parts.length - 4); i < parts.length; i++) {
            newHistory.append(parts[i]).append(",");
        }

        newHistory.append(newValue);

        getSharedPreferences("StudySessionPrefs", MODE_PRIVATE)
                .edit().putString(KEY_HISTORY, newHistory.toString()).apply();
    }

    private int calculateAverage() {
        String history = getSharedPreferences("StudySessionPrefs", MODE_PRIVATE)
                .getString(KEY_HISTORY, "");

        if (history.isEmpty()) return 0;

        String[] parts = history.split(",");
        int sum = 0;

        for (String p : parts) {
            sum += Integer.parseInt(p);
        }

        return sum / parts.length;
    }
}