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

        BottomNavigationHelper.setupNotificationFab(this, R.id.fab_notifications, R.id.bottomNavigationView);

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
                        Log.d(TAG, "Document data: " + document.getData());

                        Boolean isAdmin = document.getBoolean("admin");
                        Log.d(TAG, "Admin flag = " + isAdmin);

                        if (Boolean.TRUE.equals(isAdmin)) {
                            // Admin user
                            Log.d(TAG, "Redirecting to AdminDashboardActivity");
                            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // Normal user (entrant or organizer)
                            Log.d(TAG, "Redirecting to ProfileActivity");
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        // No profile exists for this device
                        Log.d(TAG, "No profile found — redirecting to WelcomeEntrantActivity");
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
