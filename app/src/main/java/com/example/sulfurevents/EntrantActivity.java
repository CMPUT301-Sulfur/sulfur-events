package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the entrant activity screen.
 * It allows entrants to view available events, see notifications,
 * and navigate to other sections using the bottom navigation bar.
 */
public class EntrantActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private String deviceID;
    private Button viewGuidelines;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView eventLabel;
    private BottomNavigationView bottomNavigationView;

    private EventAdapter eventAdapter;
    private List<EventModel> eventList;


    /**
     * Called when the activity is created.
     * Initializes Firestore, loads events, and sets up UI elements.
     * @param savedInstanceState The saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.entrant_activity);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.waiting_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        checkForNotifications();


        // Initialize views
        viewGuidelines = findViewById(R.id.btn_lottery_guidelines);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.events_empty);
        eventLabel = findViewById(R.id.event_label);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        // Setup RecyclerView
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);


        // Setup Bottom Navigation
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);


        // Button listener
        viewGuidelines.setOnClickListener(v -> {
            startActivity(new Intent(EntrantActivity.this, LotteryGuidelinesActivity.class));
        });

        BottomNavigationHelper.setupNotificationFab(this, R.id.fab_notifications, R.id.bottomNavigationView);

        // Load available events
        loadJoinableEvents();
    }

    /**
     * Checks the user's Firestore notifications collection for unread updates.
     * Displays a message if new lottery updates are found.
     */
    private void checkForNotifications() {
        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        Toast.makeText(this, "You have " + q.size() + " lottery update(s). Open the event to respond.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Loads joinable events from the Firestore "Events" collection.
     * Updates the RecyclerView or shows a message if no events are available.
     */
    private void loadJoinableEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        eventLabel.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);


        db.collection("Events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    eventList.clear();


                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setText("No joinable events right now.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        eventLabel.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);


                        for (QueryDocumentSnapshot document : querySnapshot) {
                            EventModel event = document.toObject(EventModel.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading events: " + e.getMessage());
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Called when the activity resumes.
     * Currently used to refresh the UI if needed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationHelper.updateNavHighlighting(bottomNavigationView, this);
    }
}
