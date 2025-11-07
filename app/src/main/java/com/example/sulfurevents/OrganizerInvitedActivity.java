package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OrganizerInvitedActivity
 * <p>
 * US 02.05.03 – Draw replacement (manual)<br>
 * US 02.06.04 – Cancel entrants who didn’t sign up
 *
 * <p>Displays the current "invited" (pending) entrants and lets the organizer:
 * <ul>
 *   <li>Cancel selected invited entrants (moves IDs from {@code invited_list} → {@code cancelled_list}).</li>
 *   <li>Manually draw replacements from the waiting list to fill any open slots.</li>
 * </ul>
 *
 * <h3>Firestore</h3>
 * <ul>
 *   <li>{@code Events/{eventId}/invited_list}</li>
 *   <li>{@code Events/{eventId}/waiting_list}</li>
 *   <li>{@code Events/{eventId}/enrolled_list}</li>
 *   <li>{@code Events/{eventId}/cancelled_list}</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>No auto-fill on decline/cancel; organizer triggers draws manually.</li>
 *   <li>Capacity may be stored as numeric {@code capacity} or string {@code limitGuests}/{@code maxCapacity}.</li>
 *   <li>List fields are read in snake/camel case for robustness.</li>
 * </ul>
 */
public class OrganizerInvitedActivity extends AppCompatActivity {

    // --- Firebase + identifiers ---
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    // --- UI ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private OrganizerInvitedAdapter adapter;

    // --- Data for adapter (parallel lists) ---
    private final List<User> users = new ArrayList<>();
    private final List<String> deviceIds = new ArrayList<>();

    /**
     * Initializes UI and loads the invited list. Wires up cancel/draw actions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_invited_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        // Optional: fetch event name for notifications
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        eventName = doc.getString("eventName");
                    }
                });

        // Back arrow
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        // Recycler + adapter
        recyclerView = findViewById(R.id.rvInvited);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerInvitedAdapter(users, deviceIds);
        recyclerView.setAdapter(adapter);

        // Cancel selected entrants
        Button btnCancel = findViewById(R.id.btnCancelSelected);
        btnCancel.setOnClickListener(v -> cancelSelected());

        // Manual draw to fill all current open slots (replacement invites)
        Button btnDrawOne = findViewById(R.id.btnDrawOneReplacement);
        btnDrawOne.setOnClickListener(v -> drawReplacementsFillOpenSlots());

        loadInvited();
    }

    /**
     * Loads the current {@code invited_list} and triggers profile resolution.
     */
    private void loadInvited() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        users.clear();
        deviceIds.clear();
        adapter.notifyDataSetChanged();

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    List<String> invited = getStringList(doc, "invited_list", "invitedList");
                    handleInvited(invited);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load invited list.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Resolves each invited deviceId to a {@link User} profile (if available) and populates the list.
     *
     * @param ids the invited device IDs (nullable/empty tolerant)
     */
    private void handleInvited(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No invited entrants.");
            return;
        }

        deviceIds.addAll(ids);
        final int total = ids.size();
        final int[] done = {0}; // async completion counter

        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        users.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        // Show placeholder row even if profile is missing
                        users.add(extractUser(id, null));
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a profile doc into a displayable {@link User}, falling back to "(Unnamed)".
     */
    private User extractUser(String id, DocumentSnapshot doc) {
        User u = new User();
        if (doc != null && doc.exists()) {
            try {
                User fromDb = doc.toObject(User.class);
                if (fromDb != null) u = fromDb;
            } catch (Exception ignored) {}
        }
        if (u.getName() == null || u.getName().isEmpty()) u.setName("(Unnamed)");
        return u;
    }

    /**
     * Finalizes population of the invited list and toggles empty state.
     */
    private void finishPopulate() {
        progressBar.setVisibility(View.GONE);
        if (users.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No invited entrants.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * US 02.06.04 — Cancels the currently selected invited entrants.
     * <p>Moves IDs from {@code invited_list} to {@code cancelled_list} and sends a simple in-app notification.
     */
    private void cancelSelected() {
        Set<String> selected = adapter.getSelectedIds();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Select at least one entrant.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> chosen = new ArrayList<>(new HashSet<>(selected));
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.update(
                "invited_list", FieldValue.arrayRemove(chosen.toArray()),
                "cancelled_list", FieldValue.arrayUnion(chosen.toArray())
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cancelled " + chosen.size() + " entrant(s).", Toast.LENGTH_LONG).show();
            adapter.clearSelection();
            loadInvited(); // refresh list
            sendNotChosenNotifications(chosen);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Sends lightweight in-app "not selected" notifications under {@code Profiles/{id}/notifications}.
     */
    private void sendNotChosenNotifications(List<String> cancelledIds) {
        for (String id : cancelledIds) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("eventId", eventId);
            notif.put("eventName", eventName != null ? eventName : "Event");
            notif.put("message", "You were not selected for " +
                    (eventName != null ? eventName : "this event") + ".");
            notif.put("timestamp", System.currentTimeMillis());
            notif.put("read", false);
            db.collection("Profiles").document(id)
                    .collection("notifications")
                    .add(notif);
        }
    }

    // ===== Helper methods =====

    /**
     * Tolerant getter for list fields that may exist under multiple keys.
     *
     * @param doc  Firestore document snapshot
     * @param keys candidate field names (first valid wins)
     * @return non-null list (empty list if not found)
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(DocumentSnapshot doc, String... keys) {
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof List) {
                try { return (List<String>) v; } catch (ClassCastException ignored) {}
            }
        }
        return new ArrayList<>();
    }

    /**
     * Reads capacity from numeric {@code capacity} or string {@code limitGuests}/{@code maxCapacity}.
     *
     * @param doc Firestore event document
     * @return capacity as int; 0 if absent/invalid
     */
    private int getCapacity(DocumentSnapshot doc) {
        Number capNum = doc.getLong("capacity");
        if (capNum != null) return capNum.intValue();
        String cap = doc.getString("limitGuests");
        if (cap == null) cap = doc.getString("maxCapacity");
        try { return Integer.parseInt(cap); } catch (Exception ignored) { return 0; }
    }

    /**
     * US 02.05.03 — Manual draw to fill all currently open slots with new invites.
     * <p>Randomly samples from {@code waiting_list} (excluding those already invited/enrolled),
     * up to {@code open = capacity - invited.size() - enrolled.size()}, and moves them to {@code invited_list}.
     */
    private void drawReplacementsFillOpenSlots() {
        final DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> waiting  = getStringList(doc, "waiting_list", "waitingList");
            List<String> invited  = getStringList(doc, "invited_list", "invitedList");
            List<String> enrolled = getStringList(doc, "enrolled_list", "enrolledList");
            if (waiting  == null) waiting  = new ArrayList<>();
            if (invited  == null) invited  = new ArrayList<>();
            if (enrolled == null) enrolled = new ArrayList<>();

            int capacity = getCapacity(doc);
            int open = Math.max(0, capacity - invited.size() - enrolled.size());
            if (open <= 0) {
                Toast.makeText(this, "No open slots to fill.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (waiting.isEmpty()) {
                Toast.makeText(this, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build eligible pool = waiting MINUS already invited or enrolled
            ArrayList<String> pool = new ArrayList<>();
            for (String id : waiting) {
                if (!invited.contains(id) && !enrolled.contains(id)) pool.add(id);
            }
            if (pool.isEmpty()) {
                Toast.makeText(this, "No eligible entrants to invite.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Randomly choose up to 'open' entrants
            java.util.Collections.shuffle(pool);
            List<String> chosen = pool.subList(0, Math.min(open, pool.size()));

            // Move waiting → invited
            eventRef.update(
                    "waiting_list", FieldValue.arrayRemove(chosen.toArray()),
                    "invited_list", FieldValue.arrayUnion(chosen.toArray())
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Invited " + chosen.size() + " replacement(s).", Toast.LENGTH_SHORT).show();
                loadInvited(); // refresh UI
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to draw replacements: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show()
        );
    }
}





