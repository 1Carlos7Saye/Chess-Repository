package com.marwadi.finalprojectchess;// Make sure this matches your project package name

<<<<<<< HEAD
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
=======
>>>>>>> nyasha-version
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        // 1. Load the Slide Animation for each button
// We create separate animation objects so they can have different start times
        Animation slide1 = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slide2 = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slide3 = AnimationUtils.loadAnimation(this, R.anim.slide_in);

// 2. Set the Delays (in milliseconds)
// Button 1 starts immediately
        slide2.setStartOffset(300); // Button 2 waits 0.3 seconds
        slide3.setStartOffset(600); // Button 3 waits 0.6 seconds

// 3. Find buttons and start the staggered animation
        findViewById(R.id.btnVsComputer).startAnimation(slide1);
        findViewById(R.id.btnPlay).startAnimation(slide2);
        findViewById(R.id.btnSettings).startAnimation(slide3);


        TextView mainTitle = findViewById(R.id.tvMainTitle);

        // 2. Load the pulse animation you created in res/anim
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);

        // 3. Start the animation
        mainTitle.startAnimation(pulse);

=======
>>>>>>> nyasha-version
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
