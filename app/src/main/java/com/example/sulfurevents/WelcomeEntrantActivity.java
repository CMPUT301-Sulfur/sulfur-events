package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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
 * this information to Firestore under the "Profiles" collection.
 * After successful registration, the user is redirected to ProfileActivity.
 */
public class WelcomeEntrantActivity extends AppCompatActivity {
    /** Button to submit the registration form */
    private Button submitButton;

    /** Input fields for user information */
    private TextInputEditText nameInput, emailInput, phoneInput;

    /** Firestore database instance */
    private FirebaseFirestore db;

    /** Unique device identifier used as the user's profile ID */
    private String deviceId;

    /**
     * Called when the activity is first created.
     * Initializes the UI, retrieves the device ID, and sets up the submit button.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
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
        String passedId = getIntent().getStringExtra("deviceId");
        if (passedId != null && !passedId.isEmpty()) {
            deviceId = passedId;
        } else {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        initializeViews();
        setupSubmitButton();

        BottomNavigationHelper.setupNotificationFab(
                this,
                R.id.fab_notifications,
                R.id.bottomNavigationView
        );
    }

    /**
     * Initializes all view components by finding them in the layout.
     * This includes the submit button and all input fields.
     */
    private void initializeViews() {
        submitButton = findViewById(R.id.submit_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    /**
     * Sets up the submit button with a click listener.
     * When clicked, validates the input fields and saves the user profile to Firestore.
     * Required fields are name and email; phone is optional.
     * On successful save, navigates to ProfileActivity.
     * On failure, displays an error message.
     */
    private void setupSubmitButton() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                boolean isAdmin = false;
                User newUser = new User(deviceId, name, email, phone, isAdmin);

                DocumentReference docRef = db.collection("Profiles").document(newUser.getDeviceId());
                docRef.set(newUser)
                        .addOnSuccessListener(unused -> {
                            Intent intent = new Intent(WelcomeEntrantActivity.this, ProfileActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // show error on current screen
                            emailInput.setError("Failed to save profile. Try again.");
                        });
            }
        });
    }
}
