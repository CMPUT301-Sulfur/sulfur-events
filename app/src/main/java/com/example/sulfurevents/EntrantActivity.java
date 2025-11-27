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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class defines the entrant activity screen.
 * It allows entrants to view available events, see notifications,
 * filter events based on interests and availability,
 * and navigate to other sections using the bottom navigation bar.
 */
public class EntrantActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceID;
    private Button viewGuidelines;
    private Button filterButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView eventLabel;
    private BottomNavigationView bottomNavigationView;

    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private List<EventModel> filteredEventList;

    // Filter variables
    private String filterKeyword = null;
    private String filterLocation = null;
    private String filterStartDate = null;
    private String filterEndDate = null;

    // Activity result launcher for filter activity
    private ActivityResultLauncher<Intent> filterActivityLauncher;

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
        filterButton = findViewById(R.id.filter_button);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.events_empty);
        eventLabel = findViewById(R.id.event_label);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup RecyclerView with filtered list
        eventList = new ArrayList<>();
        filteredEventList = new ArrayList<>();
        eventAdapter = new EventAdapter(filteredEventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);

        // Setup Bottom Navigation
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);
        bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);

        // Set up filter activity launcher
        filterActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        filterKeyword = data.getStringExtra("filterKeyword");
                        filterLocation = data.getStringExtra("filterLocation");
                        filterStartDate = data.getStringExtra("filterStartDate");
                        filterEndDate = data.getStringExtra("filterEndDate");

                        applyFilters();

                        // Show toast with active filters
                        if (hasActiveFilters()) {
                            Toast.makeText(this, "Filters applied successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Button listeners
        viewGuidelines.setOnClickListener(v -> {
            startActivity(new Intent(EntrantActivity.this, LotteryGuidelinesActivity.class));
        });

        filterButton.setOnClickListener(v -> openFilterActivity());

        BottomNavigationHelper.setupNotificationFab(this, R.id.fab_notifications, R.id.bottomNavigationView);

        // Load available events
        loadJoinableEvents();
    }

    /**
     * Opens the filter activity with current filter values
     */
    private void openFilterActivity() {
        Intent intent = new Intent(this, EventFilterActivity.class);

        // Pass current filters to the filter activity
        if (filterKeyword != null) {
            intent.putExtra("filterKeyword", filterKeyword);
        }
        if (filterLocation != null) {
            intent.putExtra("filterLocation", filterLocation);
        }
        if (filterStartDate != null && filterEndDate != null) {
            intent.putExtra("filterStartDate", filterStartDate);
            intent.putExtra("filterEndDate", filterEndDate);
        }

        filterActivityLauncher.launch(intent);
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

                        // Apply any active filters
                        applyFilters();
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
     * Applies all active filters to the event list
     */
    private void applyFilters() {
        filteredEventList.clear();

        for (EventModel event : eventList) {
            if (matchesFilters(event)) {
                filteredEventList.add(event);
            }
        }

        eventAdapter.notifyDataSetChanged();

        // Update UI visibility
        if (filteredEventList.isEmpty() && !eventList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No events match your filters. Try adjusting your criteria.");
            recyclerView.setVisibility(View.GONE);
            eventLabel.setVisibility(View.VISIBLE);
        } else if (filteredEventList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No joinable events right now.");
            recyclerView.setVisibility(View.GONE);
            eventLabel.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            eventLabel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks if an event matches all active filters
     */
    private boolean matchesFilters(EventModel event) {
        // Keyword filter (searches in event name and description)
        if (filterKeyword != null && !filterKeyword.isEmpty()) {
            String keyword = filterKeyword.toLowerCase();
            String eventName = event.getEventName() != null ?
                    event.getEventName().toLowerCase() : "";
            String eventDesc = event.getDescription() != null ?
                    event.getDescription().toLowerCase() : "";

            if (!eventName.contains(keyword) && !eventDesc.contains(keyword)) {
                return false;
            }
        }

        // Location filter
        if (filterLocation != null && !filterLocation.isEmpty()) {
            String location = event.getLocation() != null ?
                    event.getLocation().toLowerCase() : "";

            if (!location.contains(filterLocation.toLowerCase())) {
                return false;
            }
        }

        // Date filter (checks if event falls within user's availability window)
        if (filterStartDate != null && filterEndDate != null) {
            if (!matchesDateFilter(event)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if event dates fall within the user's availability window
     * An event matches if it overlaps with the user's availability period
     */
    private boolean matchesDateFilter(EventModel event) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

            Date userStartDate = df.parse(filterStartDate);
            Date userEndDate = df.parse(filterEndDate);

            String eventStartStr = event.getStartDate();
            String eventEndStr = event.getEndDate();

            if (eventStartStr == null || eventEndStr == null) {
                return false;
            }

            Date eventStartDate = df.parse(eventStartStr);
            Date eventEndDate = df.parse(eventEndStr);

            if (userStartDate == null || userEndDate == null ||
                    eventStartDate == null || eventEndDate == null) {
                return false;
            }

            // Event should overlap with user's availability window
            // Event starts before user's availability ends AND event ends after user's availability starts
            return !eventStartDate.after(userEndDate) && !eventEndDate.before(userStartDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if any filters are currently active
     */
    private boolean hasActiveFilters() {
        return (filterKeyword != null && !filterKeyword.isEmpty()) ||
                (filterLocation != null && !filterLocation.isEmpty()) ||
                (filterStartDate != null && filterEndDate != null);
    }

    /**
     * Called when the activity resumes.
     * Currently used to refresh the UI if needed.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }
}
