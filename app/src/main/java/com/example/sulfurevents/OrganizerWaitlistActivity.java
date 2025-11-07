package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;

/**
 * OrganizerWaitlistActivity
 * <p>
 * US 02.05.01 — Notify chosen entrants to sign up (random sample from waiting list).
 * <br><br>
 * This screen shows the event’s waiting list and lets an organizer send invitations
 * by randomly sampling from the waiting pool up to the number of currently-open seats:
 * <pre>
 * open = capacity - invited.size() - enrolled.size()
 * </pre>
 *
 * <h3>Firestore Shape</h3>
 * <ul>
 *   <li>Events/{eventId}/waiting_list (or waitingList) — device IDs of entrants waiting</li>
 *   <li>Events/{eventId}/invited_list (or invitedList) — pending invited device IDs</li>
 *   <li>Events/{eventId}/enrolled_list (or enrolledList) — accepted/enrolled device IDs</li>
 *   <li>Events/{eventId}/capacity OR limitGuests/maxCapacity — maximum seats</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Field names are read tolerantly in snake/camel for teammate compatibility.</li>
 *   <li>Already invited or enrolled users are excluded defensively from the draw pool.</li>
 *   <li>Draw-replacement (after cancellations) is handled on the Invited screen, not here.</li>
 * </ul>
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    // --- Firebase + State ---
    private FirebaseFirestore db;
    private String eventId;
    private String eventName; // optional, used for in-app notifications

    // --- UI ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private Button btnSend;

    // --- Adapter/Data ---
    private OrganizerWaitlistAdapter adapter;
    private final List<User> entrants = new ArrayList<>();
    private final List<String> entrantIds = new ArrayList<>();

    // --- Firestore constants ---
    private static final String EVENTS = "Events";
    private static final String ALT_EVENTS = "events";
    private static final String WAITING_SNAKE = "waiting_list";
    private static final String WAITING_CAMEL = "waitingList";

    /**
     * Binds UI, fetches event info, and loads the waiting list.
     * Sets up the "Send Invites" action to perform random sampling.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_waitlist_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        // Fetch optional event name for notifications
        db.collection(EVENTS).document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        eventName = doc.getString("eventName");
                    }
                });

        // UI wiring
        ImageButton back = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvWaitlist);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);
        btnSend = findViewById(R.id.btnSendSelectedInvites);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerWaitlistAdapter(entrants, entrantIds);
        recyclerView.setAdapter(adapter);

        back.setOnClickListener(v -> finish());

        // Randomly invite up to currently-open capacity
        btnSend.setOnClickListener(v -> sendRandomInvites());

        loadWaitlist();
    }

    /**
     * Loads the waiting list IDs for this event.
     * Tries primary collection (Events) and falls back to (events).
     */
    private void loadWaitlist() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        entrants.clear();
        entrantIds.clear();
        adapter.notifyDataSetChanged();

        db.collection(EVENTS).document(eventId).get()
                .addOnSuccessListener(doc -> {
                    List<String> list = getStringList(doc, WAITING_SNAKE, WAITING_CAMEL);
                    if (!doc.exists() || list == null) {
                        // Fallback to old collection
                        db.collection(ALT_EVENTS).document(eventId).get()
                                .addOnSuccessListener(alt ->
                                        handleWaitlist(getStringList(alt, WAITING_SNAKE, WAITING_CAMEL)))
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
     * Resolves each waiting-list deviceId to a profile (if present) and populates the adapter.
     *
     * @param ids waiting-list device IDs (nullable/empty tolerant)
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
        final int[] done = {0}; // async completion counter

        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        entrants.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        // If profile is missing/corrupt, still show a placeholder row
                        entrants.add(extractUser(id, null));
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a (possibly null) profile document to a displayable {@link User}.
     * Falls back to "(Unnamed)" if no name is present.
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
        return u;
    }

    /**
     * Finalizes the list population and toggles empty state.
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
     * Randomly invites a batch of entrants up to the number of open seats.
     * <p>
     * open = capacity - invited.size() - enrolled.size()
     * <br>
     * Moves selected IDs from waiting_list → invited_list in a single update.
     * Also sends simple in-app notifications under Profiles/{id}/notifications.
     */
    private void sendRandomInvites() {
        final DocumentReference eventRef = db.collection(EVENTS).document(eventId);

        eventRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tolerant list reads
            List<String> waiting  = getStringList(doc, "waiting_list", "waitingList");
            List<String> invited  = getStringList(doc, "invited_list", "invitedList");
            List<String> enrolled = getStringList(doc, "enrolled_list", "enrolledList");
            if (waiting == null)  waiting  = new ArrayList<>();
            if (invited == null)  invited  = new ArrayList<>();
            if (enrolled == null) enrolled = new ArrayList<>();

            // Capacity from numeric or string fields
            int capacity = getCapacity(doc);

            // Seats still available to invite
            int available = Math.max(0, capacity - invited.size() - enrolled.size());
            if (available <= 0) {
                Toast.makeText(this, "No available slots to invite.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (waiting.isEmpty()) {
                Toast.makeText(this, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Eligible pool = waiting minus already invited/enrolled
            ArrayList<String> pool = new ArrayList<>();
            for (String id : waiting) {
                if (!invited.contains(id) && !enrolled.contains(id)) pool.add(id);
            }
            if (pool.isEmpty()) {
                Toast.makeText(this, "No eligible entrants to invite.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Randomly sample up to "available" from the pool
            Collections.shuffle(pool);
            List<String> chosen = pool.subList(0, Math.min(available, pool.size()));

            // Move waiting → invited in one atomic update
            eventRef.update(
                    "waiting_list", FieldValue.arrayRemove(chosen.toArray()),
                    "invited_list", FieldValue.arrayUnion(chosen.toArray())
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Randomly invited " + chosen.size() + " entrant(s).", Toast.LENGTH_LONG).show();
                loadWaitlist(); // Refresh UI

                // Optional lightweight in-app notifications
                if (!chosen.isEmpty()) {
                    for (String invitedId : chosen) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("eventName", eventName != null ? eventName : "Event");
                        notif.put("type", "INVITED");
                        notif.put("message", "You were selected for " +
                                (eventName != null ? eventName : "this event") +
                                ". Open the event to accept or decline.");
                        notif.put("timestamp", System.currentTimeMillis());
                        notif.put("read", false);

                        db.collection("Profiles")
                                .document(invitedId)
                                .collection("notifications")
                                .add(notif);
                    }
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to update invites: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // ===== Helpers =====

    /**
     * Tolerant getter for a list field that may exist under multiple keys.
     *
     * @param doc  Firestore document
     * @param keys Candidate field names (first present wins)
     * @return List of strings or null if none found/type mismatch
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(DocumentSnapshot doc, String... keys) {
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof List) {
                try { return (List<String>) v; } catch (ClassCastException ignored) {}
            }
        }
        return null;
    }

    /**
     * Reads capacity from either numeric "capacity" or string "limitGuests"/"maxCapacity".
     *
     * @param doc Firestore event doc
     * @return capacity int, or 0 if absent/invalid
     */
    private int getCapacity(DocumentSnapshot doc) {
        // Prefer numeric "capacity"
        Number capNum = doc.getLong("capacity");
        if (capNum != null) return capNum.intValue();

        // Fallback to string fields
        String cap = doc.getString("limitGuests");
        if (cap == null) cap = doc.getString("maxCapacity");
        try { return Integer.parseInt(cap); } catch (Exception ignored) { return 0; }
    }
}






