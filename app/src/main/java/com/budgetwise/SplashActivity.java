package com.budgetwise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SplashActivity started");
        
        // Keep the splash screen on-screen for longer period
        splashScreen.setKeepOnScreenCondition(() -> true);
        
        // Initialize app and navigate to main activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Ensure application is properly initialized
                BudgetWiseApplication app = BudgetWiseApplication.getInstance();
                if (app != null) {
                    Log.d(TAG, "App initialized successfully");
                } else {
                    Log.w(TAG, "App instance is null");
                }
                
                // Navigate to main activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Error during splash initialization", e);
                // Still try to start main activity
                try {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to start MainActivity", ex);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }
}