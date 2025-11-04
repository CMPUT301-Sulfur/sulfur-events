package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.sulfurevents.databinding.ActivityMainBinding;


import java.util.Collection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Button submitButton;
    private Button editButton;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextView nameDisplay;
    private TextView emailDisplay;
    private TextView phoneDisplay;
    private FirebaseFirestore db;
    private String deviceId;
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.OrganizerEventView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            db = FirebaseFirestore.getInstance();
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            db.collection("Profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {

                if (!documentSnapshot.exists()) {
                    setContentView(R.layout.welcome_entrant);
                    EdgeToEdge.enable(this);
                    setupInsets(R.id.welcome);
                    initializeNewUserViews();
                    setupSubmitButton();
                } else {
                    setContentView(R.layout.returning_entrant);
                    EdgeToEdge.enable(this);
                    setupInsets(R.id.profile);
                    initializeReturningUserViews();
                }
            });

            return false;
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
    }

    private void setupInsets(int viewId) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(viewId), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void setupSubmitButton() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                if (phone.isEmpty()) {
                    phone = "";
                }

                boolean isAdmin = false;

                User newUser = new User(deviceId, name, email, phone, isAdmin);
                addUser(newUser);
            }
        });
    }

    public void addUser(User user){
        DocumentReference docRef = db.collection("Profiles").document(user.getDeviceId());
        docRef.set(user);
    }

    public void updateUser(User user, String name, String email, String phone){
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        db.collection("Profiles").document(user.getDeviceId()).set(user);
    }
}