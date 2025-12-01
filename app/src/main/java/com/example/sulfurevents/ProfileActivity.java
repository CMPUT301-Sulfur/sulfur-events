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

    /** Button to navigate to the profile editing screen */
    private Button deleteButton;

    /** TextViews for displaying user profile information */
    private TextView nameDisplay, emailDisplay, phoneDisplay;

    /** Firestore database instance */
    private FirebaseFirestore db;

    /** Unique device identifier used to retrieve the user's profile */
    private String deviceId;

    /** The current user object loaded from Firestore */
    private ProfileModel currentUser;

    private Button adminButton;

    private BottomNavigationView bottomNavigationView;

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

                    currentUser = documentSnapshot.toObject(ProfileModel.class);
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
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);
        BottomNavigationHelper.setupNotificationFab(
                this,
                R.id.fab_notifications,
                R.id.bottomNavigationView
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationHelper.updateNavHighlighting(bottomNavigationView, this);
    }

    /**
     * Initializes all view components by finding them in the layout.
     * This includes the edit button, display TextViews, and bottom navigation view.
     */
    private void initializeViews() {
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
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
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create custom dialog with app color scheme
                AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Delete Profile")
                        .setMessage("Are you sure you want to delete your profile? This will:\n\n" +
                                "• Remove you from all event lists\n" +
                                "• Delete all events you've created\n" +
                                "• Permanently delete your profile\n\n" +
                                "This action cannot be undone.")
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            deleteUserCompletely();
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                // Style the dialog
                dialog.setOnShowListener(dialogInterface -> {
                    // Set background to dark gray/black
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF000000));
                    }

                    // Style buttons - Delete in red/gold, Cancel in gray
                    android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (positiveButton != null) {
                        positiveButton.setTextColor(0xFFFF4444); // Red for delete action
                    }

                    android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    if (negativeButton != null) {
                        negativeButton.setTextColor(0xFFD4AF37); // Gold
                    }

                    // Style message text
                    TextView messageView = dialog.findViewById(android.R.id.message);
                    if (messageView != null) {
                        messageView.setTextColor(0xFFFFFFFF); // White
                    }

                    // Style title
                    int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                    TextView titleView = dialog.findViewById(titleId);
                    if (titleView != null) {
                        titleView.setTextColor(0xFFD4AF37); // Gold
                    }
                });

                dialog.show();
            }
        });
    }

    /**
     * Step 1: Start the deletion process
     */
    private void deleteUserCompletely() {
        // Create a custom progress dialog with proper styling
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProfileActivity.this);

        // Create a custom view for the progress dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(ProfileActivity.this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(0xFF111111); // Dark background

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(ProfileActivity.this);
        progressBar.setIndeterminate(true);
        android.widget.LinearLayout.LayoutParams progressParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.setMargins(0, 0, 30, 0);
        progressBar.setLayoutParams(progressParams);

        android.widget.TextView textView = new android.widget.TextView(ProfileActivity.this);
        textView.setText("Deleting profile...");
        textView.setTextColor(0xFFD4AF37); // Gold
        textView.setTextSize(18);

        layout.addView(progressBar);
        layout.addView(textView);

        builder.setView(layout);
        builder.setCancelable(false);

        android.app.AlertDialog progressDialog = builder.create();

        // Style the dialog window background
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(0xFF111111)
            );
        }

        // First, remove user from all event lists
        removeUserFromEventLists(progressDialog);
    }

    /**
     * Step 2: Remove user from all event lists (waiting_list, enrolled_list, invited_list, cancelled_list)
     */
    private void removeUserFromEventLists(android.app.AlertDialog progressDialog) {
        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Remove user from all four lists
                        batch.update(doc.getReference(), "waiting_list",
                                com.google.firebase.firestore.FieldValue.arrayRemove(deviceId));
                        batch.update(doc.getReference(), "enrolled_list",
                                com.google.firebase.firestore.FieldValue.arrayRemove(deviceId));
                        batch.update(doc.getReference(), "invited_list",
                                com.google.firebase.firestore.FieldValue.arrayRemove(deviceId));
                        batch.update(doc.getReference(), "cancelled_list",
                                com.google.firebase.firestore.FieldValue.arrayRemove(deviceId));
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                // After removing from lists, delete user's events
                                deleteUserCreatedEvents(progressDialog);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                showError("Failed to remove from event lists: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Failed to query events: " + e.getMessage());
                });
    }

    /**
     * Step 3: Delete all events created by this user
     */
    private void deleteUserCreatedEvents(android.app.AlertDialog progressDialog) {
        db.collection("Events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                // Finally, delete the user's profile
                                deleteUserProfile(progressDialog);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                showError("Failed to delete events: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Failed to query user events: " + e.getMessage());
                });
    }

    /**
     * Step 4: Delete the user's profile
     */
    private void deleteUserProfile(android.app.AlertDialog progressDialog) {
        db.collection("Profiles").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();

                    // Navigate to success screen
                    Intent intent = new Intent(ProfileActivity.this, SuccessfulDeleteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Failed to delete profile: " + e.getMessage());
                });
    }

    /**
     * Shows an error message to the user
     */
    private void showError(String message) {
        AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create();

        // Style the error dialog
        dialog.setOnShowListener(dialogInterface -> {
            // Set background color
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF000000));
            }

            // Style button
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(0xFFD4AF37); // Gold
            }

            // Style message text
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(0xFFFFFFFF); // White
            }

            // Style title
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView titleView = dialog.findViewById(titleId);
            if (titleView != null) {
                titleView.setTextColor(0xFFD4AF37); // Gold
            }
        });

        dialog.show();
    }
}