package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EntrantHistoryActivity extends AppCompatActivity
        implements EventHistoryAdapter.OnHistoryClickListener {

    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String deviceId;

    private final List<NotificationItem> historyItems = new ArrayList<>();
    private EventHistoryAdapter adapter;

    private boolean isLoadingData = false; // Prevent multiple loads

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_history_activity);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        ImageButton back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        progressBar = findViewById(R.id.progressBar);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventHistoryAdapter(historyItems, this);
        rvHistory.setAdapter(adapter);

        // Setup bottom navigation if it exists
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            // Set selected item BEFORE setting up the listener to avoid triggering navigation
            bottomNav.setSelectedItemId(R.id.entrant_history_navigation);
            BottomNavigationHelper.setupBottomNavigation(bottomNav, this);
        }

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only reload if we're not currently loading
        if (!isLoadingData) {
            loadHistory();
        }
    }

    private void loadHistory() {
        if (isLoadingData) {
            return; // Prevent concurrent loads
        }

        isLoadingData = true;
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyHistory.setVisibility(View.GONE);
        historyItems.clear();
        adapter.notifyDataSetChanged();

        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isLoadingData = false;
                    progressBar.setVisibility(View.GONE);

                    if (snapshot == null || snapshot.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        tvEmptyHistory.setText(
                                "You haven't registered for any events yet.");
                        return;
                    }

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot) {
                        NotificationItem item = NotificationItem.fromDoc(doc);
                        if (item.eventId != null && !item.eventId.isEmpty()) {
                            historyItems.add(item);
                        }
                    }

                    if (historyItems.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                        tvEmptyHistory.setText(
                                "You haven't registered for any events yet.");
                    } else {
                        tvEmptyHistory.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    isLoadingData = false;
                    progressBar.setVisibility(View.GONE);
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    tvEmptyHistory.setText("Failed to load history.");
                    Toast.makeText(this,
                            "Error loading history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewEvent(NotificationItem item) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", item.eventId);
        intent.putExtra("eventName", item.eventName);
        startActivity(intent);
    }
}
