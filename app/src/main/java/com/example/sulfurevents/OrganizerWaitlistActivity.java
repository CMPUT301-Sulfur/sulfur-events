package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

// Java docs done for part 3
/**
 * Activity that allows an Organizer to view the current waiting list for a selected event.
 *
 * <p>This screen retrieves the waiting list (an array of device IDs) from the event's
 * Firestore document, then loads each entrant's profile information from the "Profiles"
 * collection. Each entrant is displayed in a RecyclerView using {@link OrganizerWaitlistAdapter}.
 *
 * <p>Firestore field/collection naming is handled in a tolerant way (supports both
 * "Events" / "events" and "waiting_list" / "waitingList" for backward compatibility).
 *
 * <p>Author: sulfur (CMPUT 301 – Part 3)
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
    private OrganizerWaitlistAdapter adapter;
    private final List<User> entrants = new ArrayList<>();
    private final List<String> entrantIds = new ArrayList<>();

    // --- Firestore collection & field names ---
    private static final String EVENTS = "Events";
    private static final String ALT_EVENTS = "events";
    private static final String WAITING = "waiting_list";
    private static final String ALT_WAITING = "waitingList";


    /**
     * Initializes the waitlist screen for the selected event.
     *
     * @param savedInstanceState Previous activity state if restored.
     *
     * Steps:
     * <ul>
     *   <li>Retrieves the event ID from the intent</li>
     *   <li>Initializes Firestore and loads basic event info</li>
     *   <li>Sets up UI elements and RecyclerView with its adapter</li>
     *   <li>Configures the button to send invitations to selected users</li>
     *   <li>Loads the waitlist data from Firestore</li>
     *   <li>Back button closes the activity</li>
     * </ul>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_waitlist_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String eventName = doc.getString("eventName"); // or whatever your field is called
                    }
                });
        // --- UI setup ---
        ImageButton back = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvWaitlist);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerWaitlistAdapter(entrants, entrantIds);
        recyclerView.setAdapter(adapter);
        Button btnSend = findViewById(R.id.btnSendSelectedInvites);
        btnSend.setOnClickListener(v -> sendInvitesForSelected());


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

    /**
     * Sends invites to the selected entrants by moving them from waiting_list → invited_list.
     * No notification documents are written (ahan can add that later).
     */
    private void sendInvitesForSelected() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(OrganizerWaitlistActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

//            String eventName = doc.getString("eventName");

            String capStr = doc.getString("limitGuests");
            int capacity = 0;
            try {
                capacity = Integer.parseInt(capStr);
            } catch (Exception ignored) { }


            @SuppressWarnings("unchecked")
            List<String> waiting = (List<String>) (doc.get("waiting_list") != null ? doc.get("waiting_list") : doc.get("waitingList"));
            if (waiting == null) waiting = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<String> enrolled = (List<String>) doc.get("enrolled_list");
            if (enrolled == null) enrolled = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<String> invited = (List<String>) doc.get("invited_list");
            if (invited == null) invited = new ArrayList<>();


            int available = capacity - enrolled.size() - invited.size();
            if (available <= 0) {
                Toast.makeText(OrganizerWaitlistActivity.this, "No available slots to invite.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Random selection Logic
            List<String> shuffled = new ArrayList<>(waiting);
            java.util.Collections.shuffle(shuffled);
            List<String> chosen = shuffled.subList(0, Math.min(available, shuffled.size()));

            eventRef.update(
                    "waiting_list", FieldValue.arrayRemove(chosen.toArray()),
                    "invited_list", FieldValue.arrayUnion(chosen.toArray())
            ).addOnSuccessListener(aVoid ->{
                Toast.makeText(this, "Randomly invited " + chosen.size() + " entrant(s).", Toast.LENGTH_LONG).show();
                loadWaitlist();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to update invites: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );


            String eventName = doc.getString("eventName");
            for (String invitedId : chosen) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("eventId", eventId);
                notif.put("eventName", eventName != null ? eventName : "Event");
                notif.put("type", "INVITED");
                notif.put("message", "You were selected for " + eventName + ". Tap the event to accept or decline.");
                notif.put("timestamp", System.currentTimeMillis());
                notif.put("read", false);

                sendNotifIfEnabled(invitedId, notif);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(OrganizerWaitlistActivity.this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

    }

    private void sendNotifIfEnabled(String targetId, Map<String, Object> notif) {
        db.collection("Profiles").document(targetId).get()
                .addOnSuccessListener(doc -> {
                    Boolean enabled = doc.getBoolean("notificationsEnabled");
                    if (enabled == null || enabled) {
                        db.collection("Profiles")
                                .document(targetId)
                                .collection("notifications")
                                .add(notif);
                    }
                });
    }





}