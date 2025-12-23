package com.marwadi.finalprojectchess;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("ChessPrefs", MODE_PRIVATE);

        SwitchCompat swOrientation = findViewById(R.id.swOrientation);
        SwitchCompat swSound = findViewById(R.id.swSound);
        SwitchCompat swHelper = findViewById(R.id.swHelper);
        Button btnBack = findViewById(R.id.btnBackToMenu);

        findViewById(R.id.themeBrown).setOnClickListener(v -> saveTheme("brown"));
        findViewById(R.id.themeGreen).setOnClickListener(v -> saveTheme("green"));
        findViewById(R.id.themeBlue).setOnClickListener(v -> saveTheme("blue"));
        findViewById(R.id.themePink).setOnClickListener(v -> saveTheme("pink"));

        // Load saved choices
        swOrientation.setChecked(prefs.getBoolean("horizontal", false));
        swSound.setChecked(prefs.getBoolean("sound", true));
        swHelper.setChecked(prefs.getBoolean("helper", true));

        // Save choices and apply orientation
        swOrientation.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("horizontal", isChecked).apply();
            if (isChecked) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        swSound.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("sound", isChecked).apply());
        swHelper.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("helper", isChecked).apply());

        btnBack.setOnClickListener(v -> finish());
    }
    private void saveTheme(String themeName) {
        prefs.edit().putString("board_theme", themeName).apply();
        android.widget.Toast.makeText(this, "Theme set to: " + themeName, android.widget.Toast.LENGTH_SHORT).show();
    }
}
