package com.example.sulfurevents;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.List;

/**
 * EntrantActivity
 *
 * This screen is the main home for entrants. It supports:
 *  - US 01.01.03: Viewing a list of joinable events.
 *  - US 01.01.04: Filtering events by interests and availability
 *    (search + date filter using {@link EventFilter}).
 *  - US 01.02.03: Indirectly connected to history via notifications.
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

    // Filter UI (US 01.01.04)
    private EditText etSearch;
    private EditText etFilterDate;
    private Button btnClearFilters;

    // Data
    private EventAdapter eventAdapter;
    private final List<EventModel> eventList = new ArrayList<>();   // filtered list shown
    private final List<EventModel> allEvents = new ArrayList<>();   // full list from Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.entrant_activity);

        // Handle system bars for the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.waiting_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore + device ID
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

        etSearch = findViewById(R.id.etSearch);
        etFilterDate = findViewById(R.id.etFilterDate);
        btnClearFilters = findViewById(R.id.btnClearFilters);

        // RecyclerView setup
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);

        // Bottom navigation setup
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);
        bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);

        // Guidelines button
        viewGuidelines.setOnClickListener(v ->
                startActivity(new Intent(EntrantActivity.this, LotteryGuidelinesActivity.class)));

        // Setup filters (US 01.01.04)
        setupFilters();

        // Load joinable events (US 01.01.03)
        loadJoinableEvents();
    }

    /**
     * Checks the user's Firestore notifications collection for unread updates.
     * Displays a toast if new lottery updates are found.
     *
     * Supports US 01.04.01 / 01.04.02 indirectly.
     */
    private void checkForNotifications() {
        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        Toast.makeText(
                                this,
                                "You have " + q.size() + " lottery update(s). Open Notifications to respond.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    /**
     * Loads joinable events from the Firestore "Events" collection.
     * Populates {@link #allEvents} and then applies current filters
     * to update {@link #eventList}.
     *
     * Supports US 01.01.03.
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
                    allEvents.clear();

                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setText("No joinable events right now.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            EventModel event = document.toObject(EventModel.class);
                            event.setEventId(document.getId());
                            allEvents.add(event);
                        }
                        // After loading everything, apply any active filters
                        applyFilters();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    allEvents.clear();
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading events: " + e.getMessage());
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up listeners for search and date filter fields.
     * Implements US 01.01.04 (filter events by interests and availability).
     */
    private void setupFilters() {
        // Keyword search (name/description/location)
        etSearch.addTextChangedListener(new SimpleTextWatcher(() -> applyFilters()));

        // Date picker for availability filter
        View.OnClickListener dateClickListener = v -> showDatePicker();
        etFilterDate.setOnClickListener(dateClickListener);
        etFilterDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> {
            etSearch.setText("");
            etFilterDate.setText("");
            applyFilters();
        });
    }

    /**
     * Shows a DatePicker dialog to select the "available on" date
     * used by the availability filter.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    // We just format to string; EventFilter will parse the same format
                    int displayMonth = month + 1; // Calendar months are 0-based
                    String formatted = String.format("%02d/%02d/%04d",
                            displayMonth, dayOfMonth, year);
                    etFilterDate.setText(formatted);
                    applyFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    /**
     * Applies keyword + date filters on top of {@link #allEvents}
     * and updates the visible list {@link #eventList}.
     *
     * Uses {@link EventFilter} to encapsulate the actual logic.
     *
     * Supports US 01.01.04.
     */
    private void applyFilters() {
        eventList.clear();

        if (allEvents.isEmpty()) {
            tvEmpty.setText("No joinable events right now.");
            tvEmpty.setVisibility(View.VISIBLE);
            eventLabel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            eventAdapter.notifyDataSetChanged();
            return;
        }

        String keyword = etSearch.getText() != null
                ? etSearch.getText().toString()
                : "";
        String dateString = etFilterDate.getText() != null
                ? etFilterDate.getText().toString()
                : "";

        eventList.addAll(EventFilter.filter(allEvents, keyword, dateString));

        if (eventList.isEmpty()) {
            tvEmpty.setText("No events match your filters.");
            tvEmpty.setVisibility(View.VISIBLE);
            eventLabel.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            eventLabel.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        eventAdapter.notifyDataSetChanged();
    }

    /**
     * Called when the activity resumes.
     * You can choose to refresh events here if needed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // If you want events to refresh when coming back:
        // loadJoinableEvents();
    }

    /**
     * Small helper TextWatcher that only cares about "afterTextChanged".
     */
    private static class SimpleTextWatcher implements android.text.TextWatcher {

        private final Runnable afterChange;

        SimpleTextWatcher(Runnable afterChange) {
            this.afterChange = afterChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            if (afterChange != null) {
                afterChange.run();
            }
        }
    }
}
