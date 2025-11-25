package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class UpdateProfileActivity extends AppCompatActivity {

    private Button confirmButton;
    private TextInputEditText nameInput, emailInput, phoneInput;
    private FirebaseFirestore db;
    private String deviceId;

    private ProfileModel currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        db = FirebaseFirestore.getInstance();

        // Get device ID
        String passedId = getIntent().getStringExtra("deviceId");
        if (passedId != null && !passedId.isEmpty()) {
            deviceId = passedId;
        } else {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        initializeViews();

        // Load existing profile
        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Intent intent = new Intent(UpdateProfileActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    currentProfile = documentSnapshot.toObject(ProfileModel.class);

                    if (currentProfile != null) {
                        nameInput.setText(currentProfile.getName());
                        emailInput.setText(currentProfile.getEmail());
                        phoneInput.setText(currentProfile.getPhone());
                    }

                    setupConfirmButton();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initializeViews() {
        confirmButton = findViewById(R.id.confirm_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            if (currentProfile == null) return;

            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

            // Keep old values if left blank
            if (name.isEmpty()) name = currentProfile.getName();
            if (email.isEmpty()) email = currentProfile.getEmail();
            if (phone.isEmpty()) phone = currentProfile.getPhone();

            currentProfile.setName(name);
            currentProfile.setEmail(email);
            currentProfile.setPhone(phone);

            // MERGE → keeps isAdmin, isOrganizer, isEntrant
            db.collection("Profiles").document(deviceId)
                    .set(currentProfile, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(UpdateProfileActivity.this, ProfileActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
