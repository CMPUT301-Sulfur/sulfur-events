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

/**
 * Activity for updating an existing user profile.
 * Allows users to edit their name, email, and phone number while preserving
 * their role flags (isAdmin, isOrganizer, isEntrant) using Firestore merge.
 * If the profile doesn't exist, redirects to WelcomeEntrantActivity for profile creation.
 */
public class UpdateProfileActivity extends AppCompatActivity {

    private Button confirmButton;
    private TextInputEditText nameInput, emailInput, phoneInput;
    private FirebaseFirestore db;
    private String deviceId;

    private ProfileModel currentProfile;

    /**
     * Called when the activity is created.
     * Initializes Firestore, retrieves the device ID, loads the existing profile,
     * and sets up the UI components and notification FAB.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Get device ID from intent or use Android device ID as fallback
        String passedId = getIntent().getStringExtra("deviceId");
        if (passedId != null && !passedId.isEmpty()) {
            deviceId = passedId;
        } else {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        // Initialize UI components
        initializeViews();

        // Load existing profile from Firestore
        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If profile doesn't exist, redirect to welcome screen
                    if (!documentSnapshot.exists()) {
                        Intent intent = new Intent(UpdateProfileActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    // Convert Firestore document to ProfileModel object
                    currentProfile = documentSnapshot.toObject(ProfileModel.class);

                    // Populate input fields with existing profile data
                    if (currentProfile != null) {
                        nameInput.setText(currentProfile.getName());
                        emailInput.setText(currentProfile.getEmail());
                        phoneInput.setText(currentProfile.getPhone());
                    }

                    // Set up the confirm button listener
                    setupConfirmButton();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    finish();
                });

        // Set up the notification floating action button
        BottomNavigationHelper.setupNotificationFab(
                this,
                R.id.fab_notifications,
                R.id.bottomNavigationView
        );
    }

    /**
     * Initializes references to UI components.
     */
    private void initializeViews() {
        confirmButton = findViewById(R.id.confirm_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    /**
     * Validates that the email contains an "@" symbol.
     * This is a basic validation to ensure the email format is valid.
     *
     * @param email The email string to validate
     * @return true if email is valid (contains "@"), false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    /**
     * Sets up the confirm button click listener.
     * When clicked, validates the input, updates the profile fields,
     * and saves the changes to Firestore using merge to preserve role flags.
     * Blank fields retain their previous values.
     */
    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            if (currentProfile == null) return;

            // Get trimmed input values
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

            // Keep old values if fields are left blank
            if (name.isEmpty()) name = currentProfile.getName();
            if (email.isEmpty()) email = currentProfile.getEmail();
            if (phone.isEmpty()) phone = currentProfile.getPhone();

            // Validate email format
            if (!isValidEmail(email)) {
                emailInput.setError("Email must contain an @ symbol");
                return;
            }

            // Update the profile model with new values
            currentProfile.setName(name);
            currentProfile.setEmail(email);
            currentProfile.setPhone(phone);

            // Save to Firestore using MERGE to preserve role flags (isAdmin, isOrganizer, isEntrant)
            db.collection("Profiles").document(deviceId)
                    .set(currentProfile, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        // Navigate back to ProfileActivity after successful update
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