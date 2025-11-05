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


import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


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
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


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
                Intent intent = new Intent(WelcomeEntrantActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
    public void addUser(User user) {
        DocumentReference docRef = db.collection("Profiles").document(user.getDeviceId());
        docRef.set(user);
    }


}
