package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
<<<<<<< HEAD

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.sulfurevents.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
=======
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {
>>>>>>> origin/main
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // simple loading screen
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        // use view binding for activity_main.xml
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        // handle insets on the root view from activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(binding.MainActivityView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firestore + device ID
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // show entrant events by default
        loadFragment(new EntrantEventsFragment());

        // bottom nav listener
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener);
    }

    private final NavigationBarView.OnItemSelectedListener bottomNavListener = item -> {
        int id = item.getItemId();

        if (id == R.id.organizer_navigation) {
            startActivity(new Intent(MainActivity.this, OrganizerActivity.class));
            return true;
        }

        if (id == R.id.home_navigation) {
            loadFragment(new EntrantEventsFragment());
            return true;
        }

        if (id == R.id.qr_scanner_navigation) {
            loadFragment(new EntrantEventsFragment()); // placeholder
            return true;
        }

        if (id == R.id.entrant_events_navigation) {
            loadFragment(new EntrantEventsFragment());
            return true;
        }

        return false;
    };

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)   // <-- matches your XML now
                .commit();
    }
}
=======
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // try server first
        db.collection("Profiles").document(deviceId)
                .get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    Intent intent;
                    if (documentSnapshot.exists()) {
                        intent = new Intent(this, ProfileActivity.class);
                    } else {
                        intent = new Intent(this, WelcomeEntrantActivity.class);
                    }
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    finish();
                });
    }
}





>>>>>>> origin/main
