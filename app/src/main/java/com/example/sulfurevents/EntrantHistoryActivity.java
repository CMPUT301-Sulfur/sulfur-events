package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the event history for an entrant.
 * Shows a chronological list of all notifications related to events
 * the user has registered for, including waiting list status,
 * lottery results, and enrollment status.
 * <p>
 * Implements US 01.02.03 – History of events registered for.
 * <p>
 * Features:
 * - Displays notifications in reverse chronological order
 * - Shows empty state when no history exists
 * - Allows navigation to event details
 * - Integrates with bottom navigation
 * - Prevents concurrent data loads with flag
 */
public class EntrantHistoryActivity extends AppCompatActivity
        implements EventHistoryAdapter.OnHistoryClickListener {

    // UI Components
    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private ProgressBar progressBar;

    // Firebase and data management
    private FirebaseFirestore db;
    private String deviceId;

    // History data
    private final List<NotificationItem> historyItems = new ArrayList<>();
    private EventHistoryAdapter adapter;

    // Flag to prevent multiple simultaneous loads
    private boolean isLoadingData = false;

    /**
     * Called when the activity is first created.
     * Initializes UI components, Firebase, and loads history data.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_history_activity);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get unique device ID for this user
        deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        // Setup back button to close activity
        ImageButton back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        // Initialize UI components
        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView with linear layout and adapter
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventHistoryAdapter(historyItems, this);
        rvHistory.setAdapter(adapter);

        // Setup bottom navigation if it exists in the layout
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            // Set selected item BEFORE setting up the listener to avoid triggering navigation
            bottomNav.setSelectedItemId(R.id.entrant_history_navigation);
            BottomNavigationHelper.setupBottomNavigation(bottomNav, this);
        }

        // Load the history data from Firestore
        loadHistory();
    }

    /**
     * Called when the activity is resumed (becomes visible to user).
     * Reloads history data if not currently loading to show any updates.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Only reload if we're not currently loading to prevent conflicts
        if (!isLoadingData) {
            loadHistory();
        }
    }

    /**
     * Loads the event history from Firestore.
     * Fetches all notifications for the current user from their
     * Firestore notifications subcollection, ordered by timestamp descending.
     * <p>
     * Shows loading spinner during fetch, displays results in RecyclerView,
     * or shows empty state if no history exists.
     */
    private void loadHistory() {
        // Prevent concurrent loads
        if (isLoadingData) {
            return;
        }

        // Set loading flag and show progress indicator
        isLoadingData = true;
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyHistory.setVisibility(View.GONE);

        // Clear existing data
        historyItems.clear();
        adapter.notifyDataSetChanged();

        // Query Firestore for user's notifications
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Most recent first
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Reset loading flag and hide progress indicator
                    isLoadingData = false;
                    progressBar.setVisibility(View.GONE);

                    // Check if any notifications exist
                    if (snapshot == null || snapshot.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        tvEmptyHistory.setText(
                                "You haven't registered for any events yet.");
                        return;
                    }

                    // Process each notification document
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot) {
                        NotificationItem item = NotificationItem.fromDoc(doc);

                        // Only add notifications with valid event IDs
                        if (item.eventId != null && !item.eventId.isEmpty()) {
                            historyItems.add(item);
                        }
                    }

                    // Show appropriate UI based on whether valid items exist
                    if (historyItems.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        tvEmptyHistory.setText(
                                "You haven't registered for any events yet.");
                    } else {
                        tvEmptyHistory.setVisibility(View.GONE);
                    }

                    // Update RecyclerView with new data
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error by showing failure message
                    isLoadingData = false;
                    progressBar.setVisibility(View.GONE);
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    tvEmptyHistory.setText("Failed to load history.");
                });
    }

    /**
     * Callback method invoked when user clicks on a history item.
     * Opens the EventDetailsActivity to show full event information.
     *
     * @param item The NotificationItem that was clicked
     */
    @Override
    public void onViewEvent(NotificationItem item) {
        // Create intent to open event details
        Intent intent = new Intent(this, EventDetailsActivity.class);

        // Pass event information to details activity
        intent.putExtra("eventId", item.eventId);
        intent.putExtra("eventName", item.eventName);

        // Start the event details activity
        startActivity(intent);
    }
}
