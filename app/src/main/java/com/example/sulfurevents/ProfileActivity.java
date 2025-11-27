package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.widget.SwitchCompat;

/**
 * ProfileActivity
 * This activity displays the user's profile information including name, email, and phone number.
 * It retrieves the user data from Firestore based on the device ID.
 * If no profile exists for the device, the user is redirected to WelcomeEntrantActivity to create one.
 * Users can navigate to UpdateProfileActivity to edit their profile information.
 * Includes bottom navigation for app-wide navigation.
 */
public class ProfileActivity extends AppCompatActivity {
    /** Button to navigate to the profile editing screen */
    private Button editButton;

    /** TextViews for displaying user profile information */
    private TextView nameDisplay, emailDisplay, phoneDisplay;

    /** Firestore database instance */
    private FirebaseFirestore db;

    /** Unique device identifier used to retrieve the user's profile */
    private String deviceId;

    /** The current user object loaded from Firestore */
    private ProfileModel currentUser;

    private Button adminButton;
    private Button deleteButton;

    private SwitchCompat notificationsSwitch;

    /**
     * Called when the activity is first created.
     * Initializes the UI, retrieves the device ID, loads the user profile from Firestore,
     * and displays the profile information. Sets up bottom navigation for app-wide navigation.
     * If the profile doesn't exist, redirects to WelcomeEntrantActivity to create a new profile.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.returning_entrant);
        EdgeToEdge.enable(this);

        // Apply insets to the root content view (was R.id.profile, which didn't exist)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
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

        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    // Load profile as ProfileModel from Firestore
                    currentUser = documentSnapshot.toObject(ProfileModel.class);
                    if (currentUser == null) {
                        // Fallback if something is wrong with the document
                        Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    // Read notificationsEnabled flag (default ON if missing)
                    Boolean enabled = documentSnapshot.getBoolean("notificationsEnabled");
                    if (enabled == null) enabled = true;
                    currentUser.setNotificationsEnabled(enabled);

                    // Wire notifications switch if present in the layout
                    if (notificationsSwitch != null) {
                        notificationsSwitch.setChecked(enabled);

                        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            db.collection("Profiles")
                                    .document(deviceId)
                                    .update("notificationsEnabled", isChecked);
                        });
                    }

                    displayInfo();
                    setupEditButton();
                    setupDeleteButton(); // keep as intended

                    adminButton.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                    });

                })
                .addOnFailureListener(e -> {
                    Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);
        BottomNavigationHelper.setupNotificationFab(
                this,
                R.id.fab_notifications,
                R.id.bottomNavigationView
        );
    }

    /**
     * Initializes all view components by finding them in the layout.
     * This includes the edit button, display TextViews, and bottom navigation view.
     */
    private void initializeViews() {
        editButton = findViewById(R.id.edit_button);
        nameDisplay = findViewById(R.id.name_display);
        emailDisplay = findViewById(R.id.email_display);
        phoneDisplay = findViewById(R.id.phone_display);
        adminButton = findViewById(R.id.admin_button);
        deleteButton = findViewById(R.id.delete_button);
        notificationsSwitch = findViewById(R.id.switch_notifications);
    }

    /**
     * Displays the user's profile information in the TextViews.
     * Shows the user's name, email, and phone number.
     * If phone number is not available, displays "Not provided".
     */
    private void displayInfo() {
        if (currentUser != null) {
            nameDisplay.setText("Name: " + currentUser.getName());
            emailDisplay.setText("Email: " + currentUser.getEmail());
            String phone = currentUser.getPhone();
            phoneDisplay.setText("Phone Number: " + (phone == null || phone.isEmpty() ? "Not provided" : phone));

            // Show admin button only if user is marked as admin
            if (currentUser.getIsAdmin()) {
                adminButton.setVisibility(View.VISIBLE);
            } else {
                adminButton.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets up the edit button with a click listener.
     * When clicked, navigates to UpdateProfileActivity where the user can modify their profile.
     * Passes the device ID to the next activity for profile retrieval.
     */
    private void setupEditButton() {
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Sets up the delete button with a confirmation dialog.
     * On confirmation, deletes the user's profile document and redirects to WelcomeEntrantActivity.
     */
    private void setupDeleteButton() {
        if (deleteButton == null) return;

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("Profiles").document(deviceId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                                    intent.putExtra("deviceId", deviceId);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        new AlertDialog.Builder(ProfileActivity.this)
                                                .setTitle("Error")
                                                .setMessage("Failed to delete profile: " + e.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}