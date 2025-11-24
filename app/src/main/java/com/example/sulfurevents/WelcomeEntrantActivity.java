package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * WelcomeEntrantActivity
 * This activity is displayed when a new user first opens the app.
 * It collects the user's name, email, and phone number, then saves
 * this information to Firestore under the "Profiles" collection
 * using the device ID as the document ID.
 * After successful registration, the user is redirected to ProfileActivity.
 */
public class WelcomeEntrantActivity extends AppCompatActivity {

    private Button submitButton;
    private TextInputEditText nameInput, emailInput, phoneInput;

    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_entrant);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Determine device ID (or use one passed from previous activity)
        String passedId = getIntent().getStringExtra("deviceId");
        if (passedId != null && !passedId.isEmpty()) {
            deviceId = passedId;
        } else {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        initializeViews();
        setupSubmitButton();
    }

    private void initializeViews() {
        submitButton = findViewById(R.id.submit_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                nameInput.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                emailInput.requestFocus();
                return;
            }

            // Create the unified ProfileModel
            ProfileModel profile = new ProfileModel(email, phone, deviceId);
            profile.setName(name);

            // Default roles for new accounts
            profile.setIsEntrant(true);
            profile.setIsOrganizer(false);
            profile.setIsAdmin(false);

            // Save using device ID as Firestore document ID
            DocumentReference docRef = db.collection("Profiles").document(deviceId);

            docRef.set(profile)
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(WelcomeEntrantActivity.this, ProfileActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            emailInput.setError("Failed to save profile. Try again.")
                    );
        });
    }
}