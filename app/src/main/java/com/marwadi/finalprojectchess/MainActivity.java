package com.marwadi.finalprojectchess;// Make sure this matches your project package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Link all three buttons
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnVsComputer = findViewById(R.id.btnVsComputer); // New button ID
        Button btnSettings = findViewById(R.id.btnSettings);

        // 2. Play vs Human (Friend)
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("isVsComputer", false); // Tell GameActivity: NO BOT
            startActivity(intent);
        });

        // 3. Play vs Computer
        btnVsComputer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("isVsComputer", true); // Tell GameActivity: ACTIVATE BOT
            startActivity(intent);
        });

        // 4. Settings
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    }
