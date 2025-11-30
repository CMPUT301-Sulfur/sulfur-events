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
 *
 * This activity is displayed the first time a new user opens the app.
 * It collects the user's basic information (name, email, phone),
 * creates a new profile in Firestore using the ProfileModel class,
 * and assigns default roles:
 *
 *      - isEntrant = true
 *      - isOrganizer = false
 *      - isAdmin = false
 *
 * After registration, the user is redirected to ProfileActivity.
 */
public class WelcomeEntrantActivity extends AppCompatActivity {

    /** Button used to submit the registration form. */
    private Button submitButton;

    /** Input fields where the user types their personal info. */
    private TextInputEditText nameInput, emailInput, phoneInput;

    /** Firestore instance used to save the new profile. */
    private FirebaseFirestore db;

    /** Unique device identifier used as the Firestore profile document ID. */
    private String deviceId;

    /**
     * Called when the activity is first created.
     * Sets up UI layout, retrieves the device ID, and initializes the submit button.
     *
     * @param savedInstanceState The saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_entrant);

        EdgeToEdge.enable(this);

        // Apply window insets (keeps UI from overlapping with system bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Retrieve device ID, either passed from another activity or directly from system
        String passedId = getIntent().getStringExtra("deviceId");
        if (passedId != null && !passedId.isEmpty()) {
            deviceId = passedId;
        } else {
            deviceId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        }

        initializeViews();
        setupSubmitButton();
    }

    /**
     * Finds and initializes all the UI components required for the screen.
     */
    private void initializeViews() {
        submitButton = findViewById(R.id.submit_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    /**
     * Validates that the email contains an "@" symbol.
     *
     * @param email The email string to validate.
     * @return true if valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    /**
     * Sets up the submit button.
     *
     * When clicked:
     *  - Validates user input (name + email required)
     *  - Creates a ProfileModel with default boolean values
     *  - Saves the profile to Firestore
     *  - Navigates to ProfileActivity on success
     */
    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {

            // Read input values safely
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

            // Validate required fields
            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                return;
            }

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                return;
            }

            // Validate email format
            if (!isValidEmail(email)) {
                emailInput.setError("Email must contain an @ symbol");
                return;
            }

            /**
             * 🔥 CREATE NEW PROFILE USING ProfileModel (NOT the old User class)
             * ProfileModel automatically sets:
             *    isEntrant = true
             *    isOrganizer = false
             *    isAdmin = false
             */
            ProfileModel newProfile = new ProfileModel(email, phone, deviceId);
            newProfile.setName(name);

            // These defaults ensure correct role assignment on first registration
            newProfile.setIsEntrant(true);
            newProfile.setIsOrganizer(false);
            newProfile.setIsAdmin(false);

            // Reference to the profile document
            DocumentReference docRef = db.collection("Profiles").document(deviceId);

            // Save to Firestore
            docRef.set(newProfile)
                    .addOnSuccessListener(unused -> {
                        // Navigate to profile screen
                        Intent intent = new Intent(
                                WelcomeEntrantActivity.this,
                                ProfileActivity.class
                        );
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Display an error message if saving fails
                        emailInput.setError("Failed to save profile. Try again.");
                    });
        });
    }
}
