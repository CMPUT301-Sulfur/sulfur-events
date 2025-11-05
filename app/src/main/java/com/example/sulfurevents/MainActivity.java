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

import com.example.sulfurevents.databinding.ActivityMainBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Button submitButton;
    private Button editButton;
    private Button notificationsButton;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextView nameDisplay;
    private TextView emailDisplay;
    private TextView phoneDisplay;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.MainActivityView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // bottom nav
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.organizer_navigation) {
                startActivity(new Intent(MainActivity.this, OrganizerActivity.class));
                return true;
            }

            if (id == R.id.qr_scanner_navigation) {
                // TODO: open QR screen
                return true;
            }

            // home / entrant
            showEntrantScreen();
            return true;
        });

        // show entrant screen on startup
        showEntrantScreen();
    }

    private void showEntrantScreen() {
        db.collection("Profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // new user
                setContentView(R.layout.welcome_entrant);
                EdgeToEdge.enable(this);
                setupInsets(R.id.welcome);
                initializeNewUserViews();
                setupSubmitButton();
            } else {
                // returning user
                setContentView(R.layout.returning_entrant);
                EdgeToEdge.enable(this);
                setupInsets(R.id.profile);
                initializeReturningUserViews();

                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");

                if (nameDisplay != null) nameDisplay.setText("Name: " + (name != null ? name : ""));
                if (emailDisplay != null) emailDisplay.setText("Email: " + (email != null ? email : ""));
                if (phoneDisplay != null) phoneDisplay.setText("Phone Number: " + (phone != null ? phone : ""));
            }
        });
    }

    private void initializeNewUserViews() {
        submitButton = findViewById(R.id.submit_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }

    private void initializeReturningUserViews() {
        editButton = findViewById(R.id.edit_button);
        nameDisplay = findViewById(R.id.name_display);
        emailDisplay = findViewById(R.id.email_display);
        phoneDisplay = findViewById(R.id.phone_display);
        notificationsButton = findViewById(R.id.notifications_button);

        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, EntrantNotificationsActivity.class);
                startActivity(i);
            });
        }
    }

    private void setupInsets(int viewId) {
        View root = findViewById(viewId);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

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

            // make user map like your User model
            Map<String, Object> profile = new HashMap<>();
            profile.put("deviceId", deviceId);
            profile.put("name", name);
            profile.put("email", email);
            profile.put("phone", phone);
            profile.put("userType", "entrant");

            DocumentReference docRef = db.collection("Profiles").document(deviceId);
            docRef.set(profile).addOnSuccessListener(unused -> {
                // show returning screen
                setContentView(R.layout.returning_entrant);
                EdgeToEdge.enable(MainActivity.this);
                setupInsets(R.id.profile);
                initializeReturningUserViews();

                if (nameDisplay != null) nameDisplay.setText("Name: " + name);
                if (emailDisplay != null) emailDisplay.setText("Email: " + email);
                if (phoneDisplay != null) phoneDisplay.setText("Phone Number: " + phone);
            });
        });
    }
}