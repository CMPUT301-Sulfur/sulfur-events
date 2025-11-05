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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // view binding for activity_main.xml
    private ActivityMainBinding binding;

    // entrant screen views (these only exist on welcome/returning layouts)
    private Button submitButton;
    private Button editButton;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextView nameDisplay;
    private TextView emailDisplay;
    private TextView phoneDisplay;

    // firestore
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the main layout with bottom nav
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        // insets for main layout root
        ViewCompat.setOnApplyWindowInsetsListener(binding.MainActivityView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // firestore + device id
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // set up bottom nav for the first time
        setupBottomNav(binding.bottomNavigationView);
    }

    /**
     * Sets up the bottom navigation listener.
     * Call this again whenever you change layouts with setContentView(...)
     */
    private void setupBottomNav(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // 1) organizer tab
            if (itemId == R.id.organizer_navigation) {
                Intent intent = new Intent(MainActivity.this, OrganizerActivity.class);
                startActivity(intent);
                return true;
            }

            // 2) view events tab (the entrant list screen we created)
            if (itemId == R.id.nav_view_events) {
                Intent intent = new Intent(MainActivity.this, EntrantViewEventsActivity.class);
                startActivity(intent);
                return true;
            }

            // 3) everything else: show entrant profile/welcome from Firestore
            showEntrantScreen();
            return true;
        });
    }

    /**
     * Fetches the profile and shows either welcome_entrant or returning_entrant
     */
    private void showEntrantScreen() {
        db.collection("Profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // user not in db yet -> show welcome screen
                setContentView(R.layout.welcome_entrant);
                EdgeToEdge.enable(this);
                setupInsets(R.id.welcome);
                initializeNewUserViews();
                setupSubmitButton();
                // this layout also has bottom nav at the bottom in your screenshot → reattach
                reattachBottomNavFromCurrentLayout();
            } else {
                // user exists → show returning screen
                setContentView(R.layout.returning_entrant);
                EdgeToEdge.enable(this);
                setupInsets(R.id.profile);
                initializeReturningUserViews();
                reattachBottomNavFromCurrentLayout();
            }
        });
    }

    /**
     * After we call setContentView(...) we lose the original binding views,
     * so we find the bottom nav in the *new* layout (welcome/returning) and attach the same logic.
     */
    private void reattachBottomNavFromCurrentLayout() {
        View navView = findViewById(R.id.bottomNavigationView);
        if (navView instanceof BottomNavigationView) {
            setupBottomNav((BottomNavigationView) navView);
        }
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

            if (phone.isEmpty()) {
                phone = "";
            }

            boolean isAdmin = false;
            User newUser = new User(deviceId, name, email, phone, isAdmin);
            addUser(newUser);
        });
    }

    public void addUser(User user) {
        DocumentReference docRef = db.collection("Profiles").document(user.getDeviceId());
        docRef.set(user);
    }

    public void updateUser(User user, String name, String email, String phone) {
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        db.collection("Profiles").document(user.getDeviceId()).set(user);
    }
}
