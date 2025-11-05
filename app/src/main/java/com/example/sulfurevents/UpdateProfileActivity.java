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
import com.google.firebase.firestore.FirebaseFirestore;


public class UpdateProfileActivity extends AppCompatActivity {
    private Button confirmButton;
    private TextInputEditText nameInput, emailInput, phoneInput;
    private FirebaseFirestore db;
    private String deviceId;
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.update), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        initializeViews();


        db.collection("Profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            setupConfirmButton();


        });


    }
    private void initializeViews() {
        confirmButton = findViewById(R.id.confirm_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
    }


    private void setupConfirmButton() {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();


                if (name.isEmpty()) {
                    name = currentUser.getName();
                }


                if (email.isEmpty()) {
                    email = currentUser.getEmail();
                }


                if (phone.isEmpty()) {
                    phone = currentUser.getPhone();
                }


                updateUser(currentUser, name, email, phone);
                Intent intent = new Intent(UpdateProfileActivity.this, ProfileActivity.class);
                startActivity(intent);


            }
        });
    }


    public void updateUser(User user, String name, String email, String phone){
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        db.collection("Profiles").document(user.getDeviceId()).set(user);
    }
}
