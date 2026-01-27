package com.marwadi.finalprojectchess;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // This replaces the old onBackPressed() and removes the red line error
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing: this blocks the back button during the splash screen
            }
        });

        // 2-second delay before moving to the Main Menu
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);

            androidx.core.app.ActivityOptionsCompat options =
                    androidx.core.app.ActivityOptionsCompat.makeCustomAnimation(
                            SplashActivity.this,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
            startActivity(intent, options.toBundle());
            finish();
        }, 2000);
    }
}