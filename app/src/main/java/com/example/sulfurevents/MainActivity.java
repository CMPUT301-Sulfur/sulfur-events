package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Main entry point of the Sulfur Events app.
 * Checks if the device already has a profile in Firestore and
 * redirects to the correct screen (Admin, Entrant Profile, or New User).
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Get this device's unique ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceId);

        checkUserProfile();
    }

    /**
     * Checks Firestore to determine what kind of user this is.
     */
    private void checkUserProfile() {
        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    Log.d(TAG, "Firestore check for " + deviceId + " => exists: " + document.exists());

                    if (document.exists()) {

                        // Profile exists → always go to ProfileActivity
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();

                    } else {
                        // No profile → go to welcome screen
                        Intent intent = new Intent(MainActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user profile", e);
                    // Optional: fallback to registration if Firestore fails
                    Intent intent = new Intent(MainActivity.this, WelcomeEntrantActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });
    }
}
