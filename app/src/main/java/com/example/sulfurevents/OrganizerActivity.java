package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String DeviceID;
    private User CurrentUser;
    private ArrayList<OrganizerEvent> organizerEvents = new ArrayList<>();
    private OrganizerEventsAdapter adapter;

    // Done for part 3
    /**
     * Activity for organizers to view and manage their created events.
     * <p>
     * On creation:
     * <ul>
     *   <li>Enables edge-to-edge layout</li>
     *   <li>Initializes Firestore and retrieves the device ID</li>
     *   <li>Fetches the organizer's profile and updates the welcome header</li>
     *   <li>Sets up the RecyclerView to display events created by this organizer</li>
     *   <li>Starts a real-time listener to load organizer events from Firestore</li>
     *   <li>Configures navigation buttons and bottom navigation bar</li>
     * </ul>
     *
     * @param savedInstanceState Previous state if the activity is being restored
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.organizer_activity);

        // Handle insets for fullscreen mode
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.OrganizerEventView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        TextView headerTitle = findViewById(R.id.HeaderTitle);

        // Fetch and display the organizer’s name from Firestore
        db.collection("Profiles").document(DeviceID).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        CurrentUser = doc.toObject(User.class);
                        if (CurrentUser != null && CurrentUser.getName() != null && !CurrentUser.getName().isEmpty()) {
                            headerTitle.setText("Welcome " + CurrentUser.getName());
                        } else {
                            headerTitle.setText("Welcome Organizer");
                        }
                    } else {
                        headerTitle.setText("Welcome Organizer");
                    }
                })
                .addOnFailureListener(e -> headerTitle.setText("Welcome Organizer"));

        // Setup RecyclerView for the organizer’s event list
        RecyclerView recyclerView = findViewById(R.id.CreatedEventsRecyclerView);
        OrganizerEventsAdapter adapter = new OrganizerEventsAdapter(this, organizerEvents);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadEventsFromFirestore(adapter);

        // Back Button (returns to MainActivity)
        ImageButton backButton = findViewById(R.id.BackButtonOrganizerEvents);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, OrganizerActivity.class);
            finish();
        });

        // Create Event Button
        Button createEventButton = findViewById(R.id.CreateEventButton);
        createEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEventActivity.class);
            startActivity(intent);
        });

        // Bottom navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);

        BottomNavigationHelper.setupNotificationFab(this, R.id.fab_notifications, R.id.bottomNavigationView);
    }



    /**
     * Loads events from the Firestore "Events" collection that match the current organizer's ID.
     * This method attaches a real-time listener, so any changes in the database will automatically
     * update the provided adapter and refresh the event list in the UI.
     *
     * @param adapter The RecyclerView adapter responsible for displaying the list of organizer events.
     *                Once the Firestore query completes or updates, this adapter will be notified
     *                so the UI reflects the latest data.
     * Firestore Query Behavior:
     *                Filters documents where the "organizerId" field matches the current device's ID.
     *                Clears the existing event list before repopulating to avoid duplicates.
     *                Converts each matching Firestore document into an {@link OrganizerEvent} object.
     *                Triggers UI update through {@code adapter.notifyDataSetChanged()} after data refresh.
     */
    private void loadEventsFromFirestore(OrganizerEventsAdapter adapter) {
        db.collection("Events")
                .whereEqualTo("organizerId", DeviceID)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    organizerEvents.clear();
                    if (snapshots != null) {
                        for (var doc : snapshots.getDocuments()) {
                            OrganizerEvent event = doc.toObject(OrganizerEvent.class);
                            if (event != null) organizerEvents.add(event);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
