package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows an Organizer to view the current waiting list for a selected event.
 *
 * <p>This screen retrieves the waiting list (an array of device IDs) from the event's
 * Firestore document, then loads each entrant's profile information from the "Profiles"
 * collection. Each entrant is displayed in a RecyclerView using {@link WaitlistAdapter}.
 *
 * <p>Firestore field/collection naming is handled in a tolerant way (supports both
 * "Events" / "events" and "waiting_list" / "waitingList" for backward compatibility).
 *
 * <p>Author: Daniel Minchenko (CMPUT 301 – Part 3)
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    // --- Firebase + State ---
    private FirebaseFirestore db;
    private String eventId;

    // --- UI components ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // --- Adapter data ---
    private WaitlistAdapter adapter;
    private final List<User> entrants = new ArrayList<>();
    private final List<String> entrantIds = new ArrayList<>();

    // --- Firestore collection & field names ---
    private static final String EVENTS = "Events";
    private static final String ALT_EVENTS = "events";
    private static final String WAITING = "waiting_list";
    private static final String ALT_WAITING = "waitingList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_waitlist_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        // --- UI setup ---
        ImageButton back = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvWaitlist);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(entrants, entrantIds);
        recyclerView.setAdapter(adapter);

        // Back button simply finishes this Activity
        back.setOnClickListener(v -> finish());

        // Load waiting list on open
        loadWaitlist();
    }

    /**
     * Loads the waiting list for the current event.
     *
     * <p>Attempts to read from "Events" first, and falls back to the older "events" collection.
     * Once IDs are retrieved, {@link #handleWaitlist(List)} fetches each entrant's profile.
     */
    private void loadWaitlist() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        entrants.clear();
        entrantIds.clear();
        adapter.notifyDataSetChanged();

        db.collection(EVENTS).document(eventId).get()
                .addOnSuccessListener(doc -> {
                    List<String> list = null;
                    if (doc.exists()) {
                        list = (List<String>) doc.get(WAITING);
                        if (list == null) list = (List<String>) doc.get(ALT_WAITING);
                    }

                    // Try the alternate collection if no data found
                    if (list == null) {
                        db.collection(ALT_EVENTS).document(eventId).get()
                                .addOnSuccessListener(alt -> handleWaitlist((List<String>)
                                        (alt.exists() ? (alt.get(WAITING) != null ? alt.get(WAITING) : alt.get(ALT_WAITING)) : null)))
                                .addOnFailureListener(e -> handleWaitlist(null));
                    } else {
                        handleWaitlist(list);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load waitlist.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles a list of device IDs retrieved from Firestore.
     * For each ID, the corresponding user profile is fetched from the "Profiles" collection.
     *
     * @param ids List of entrant device IDs (may be null or empty)
     */
    private void handleWaitlist(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No entrants on the waiting list.");
            return;
        }

        entrantIds.addAll(ids);
        final int total = ids.size();
        final int[] done = {0}; // tracks completion count

        // Fetch each profile asynchronously
        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        entrants.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        entrants.add(extractUser(id, null)); // fallback to bare ID
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a Firestore document to a {@link User} object.
     * If profile data is missing, returns a placeholder with default name and ID.
     *
     * @param id  Device ID of the entrant
     * @param doc Firestore profile document (may be null)
     * @return User object for display
     */
    private User extractUser(String id, DocumentSnapshot doc) {
        User u = new User();
        if (doc != null && doc.exists()) {
            try {
                User fromDb = doc.toObject(User.class);
                if (fromDb != null) u = fromDb;
            } catch (Exception ignored) {}
        }

        if (u.getName() == null || u.getName().isEmpty()) {
            u.setName("(Unnamed)");
        }
        // Optional: include device ID in user if your model supports it
        return u;
    }

    /**
     * Finalizes UI population once all profiles are loaded.
     * Shows "no entrants" message if empty, otherwise refreshes adapter.
     */
    private void finishPopulate() {
        progressBar.setVisibility(View.GONE);
        if (entrants.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No entrants on the waiting list.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}
