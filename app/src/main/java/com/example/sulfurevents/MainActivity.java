package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // simple loading screen
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // try server first
        db.collection("Profiles").document(deviceId)
                .get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    Intent intent;
                    if (documentSnapshot.exists()) {
                        intent = new Intent(this, ProfileActivity.class);
                    } else {
                        intent = new Intent(this, WelcomeEntrantActivity.class);
                    }
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });
    }
}





