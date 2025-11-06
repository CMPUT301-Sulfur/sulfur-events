package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private Button editButton;
    private TextView nameDisplay, emailDisplay, phoneDisplay;
    private FirebaseFirestore db;
    private String deviceId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.returning_entrant);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
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
                        // profile was deleted or never made, send to welcome
                        Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                        intent.putExtra("deviceId", deviceId);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    currentUser = documentSnapshot.toObject(User.class);
                    displayInfo();
                    setupEditButton();
                })
                .addOnFailureListener(e -> {
                    // if failure, you could show a message or send to welcome
                    Intent intent = new Intent(ProfileActivity.this, WelcomeEntrantActivity.class);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);

    }

    private void initializeViews() {
        editButton = findViewById(R.id.edit_button);
        nameDisplay = findViewById(R.id.name_display);
        emailDisplay = findViewById(R.id.email_display);
        phoneDisplay = findViewById(R.id.phone_display);
    }

    private void displayInfo() {
        if (currentUser != null) {
            nameDisplay.setText("Name: " + currentUser.getName());
            emailDisplay.setText("Email: " + currentUser.getEmail());
            String phone = currentUser.getPhone();
            phoneDisplay.setText("Phone Number: " + (phone == null || phone.isEmpty() ? "Not provided" : phone));
        }
    }

    private void setupEditButton() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });
    }
}

