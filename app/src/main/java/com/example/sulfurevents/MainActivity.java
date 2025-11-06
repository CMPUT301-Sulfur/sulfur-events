package com.example.sulfurevents;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Get unique device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Check if user profile exists for this device
        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Boolean isAdmin = document.getBoolean("admin");


                        if (Boolean.TRUE.equals(isAdmin)) {
                            // Admin user
                            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Normal user (entrant or organizer)
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }


                    } else {
                        // No profile for this device, go to new user flow
                        Intent intent = new Intent(MainActivity.this, WelcomeEntrantActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
