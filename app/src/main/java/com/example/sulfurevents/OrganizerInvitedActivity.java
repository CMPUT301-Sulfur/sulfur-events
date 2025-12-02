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
// Done for Part 3
/**
 * US 02.06.04 – Cancel entrants who didn’t sign up
 * US 02.05.03 – Draw replacement (manual and auto after cancellations)
 *
 * <p>Organizer facing screen that lists all deviceIds in {@code invited_list} (pending sign-up)
 * for a given event and lets the organizer multi-select and cancel any subset. Cancellation moves
 * each selected deviceId from {@code invited_list} to {@code cancelled_list} using atomic
 * {@link FieldValue#arrayRemove(Object...)} / {@link FieldValue#arrayUnion(Object...)} updates.
 *
 * <p>Notes:
 * <ul>
 *   <li>No notifications are sent here; this is intentionally Firestore-only so teammates can
 *       build “receive”/UI reactions later without changing this screen.</li>
 *   <li>Reuses {@link OrganizerWaitlistAdapter} and the same row layout with a CheckBox for selection.</li>
 * </ul>
 *
 * <p>Firestore usage:
 * <ul>
 *   <li>Events/{eventId}/invited_list   : List&lt;String&gt; of deviceIds currently invited</li>
 *   <li>Events/{eventId}/cancelled_list : List&lt;String&gt; of deviceIds organizer cancelled</li>
 *   <li>Profiles/{deviceId}             : optional profile document used to display name/email</li>
 * </ul>
 *
 * <p>Author: sulfur (CMPUT 301-Part 3)</p>
 */
public class OrganizerInvitedActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String organizerDeviceId;

    // UI
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // Adapter/data (parallel lists)
    private OrganizerInvitedAdapter adapter;                 // provides multi-select via CheckBox
    private final List<User> users = new ArrayList<>();
    private final List<String> deviceIds = new ArrayList<>();


    /**
     * Initializes the screen that shows users who have been invited to an event.
     * <p>
     * Loads the event ID, retrieves the event name, prepares UI elements, sets up the
     * RecyclerView with its adapter, and attaches actions for canceling invitations
     * and drawing replacement invitees. Finally, it loads the invited-user list.
     *
     * @param savedInstanceState State of the activity if restored from a previous instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_invited_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            eventId = getIntent().getStringExtra("EVENT_ID"); // optional fallback
        }
        organizerDeviceId = android.provider.Settings.Secure.getString(
                getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        eventName = doc.getString("eventName");
                    }
                });

        // Back arrow
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvInvited);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerInvitedAdapter(users, deviceIds);
        recyclerView.setAdapter(adapter);

        // action: cancel selected invited entrants
        Button btnCancel = findViewById(R.id.btnCancelSelected);
        btnCancel.setOnClickListener(v -> cancelSelected());

        // US 02.05.03 draw replacment
        Button btnDrawOne = findViewById(R.id.btnDrawOneReplacement);
        btnDrawOne.setOnClickListener(v -> drawReplacements(1));


        loadInvited();
    }

    /**
     * Loads {@code invited_list} for this event and begins resolving each deviceId to a
     * profile document for display. Shows a empty state if nothing is invited
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
     * Best effort retrieval of a string list field that may appear under multiple keys,
     * returning a non-null list when the field is missing or has an unexpected type.
     *
     * @param doc  Firestore document snapshot
     * @param keys Fallback field names in priority order
     * @return a non-null list instance
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(DocumentSnapshot doc, String... keys) {
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof List) {
                try {
                    return (List<String>) v;
                } catch (ClassCastException ignored) {
                    // fall through to return empty list
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * After reading {@code invited_list}, resolve each deviceId to a profile (if present)
     * and then render via the adapter
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
        final int[] done = {0};

        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        users.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        users.add(extractUser(id, null)); // fallback to placeholder row
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a profile document (if present) to a {@link User} for display.
     * Falls back to a placeholder name if profile data is missing or invalid.
     *
     * @param id  deviceId key used in Profiles
     * @param doc profile snapshot (may be null)
     * @return a non-null {@code User} object
     */
    private User extractUser(String id, DocumentSnapshot doc) {
        User u = new User();
        if (doc != null && doc.exists()) {
            try {
                User fromDb = doc.toObject(User.class);
                if (fromDb != null) u = fromDb;
            } catch (Exception ignored) {
                // keep default
            }
        }
        if (u.getName() == null || u.getName().isEmpty()) {
            u.setName("(Unnamed)");
        }
        return u;
    }

    /**
     * Finalizes population by hiding progress, toggling empty state visibility,
     * and notifying the adapter.
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
     *
     * <p>Moves selected deviceIds from {@code invited_list} to {@code cancelled_list}
     * using a single update, Selection is cleared and the list is reloaded upon success.
     * No notifications are emitted in this version yet
     */
    private void cancelSelected() {
        Set<String> selected = adapter.getSelectedIds();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Select at least one entrant.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Defensive copy and de-dupe in case the adapter selection had duplicates.
        final List<String> chosen = new ArrayList<>(new HashSet<>(selected));

        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.update(
                "invited_list", FieldValue.arrayRemove(chosen.toArray()),
                "cancelled_list", FieldValue.arrayUnion(chosen.toArray())
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cancelled " + chosen.size() + " entrant(s).", Toast.LENGTH_LONG).show();
            adapter.clearSelection();
            loadInvited(); // refresh to reflect removals
            for (String cancelledId : chosen) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("eventId", eventId);
                notif.put("eventName", eventName != null ? eventName : "Event");
                notif.put("type", "NOT_SELECTED");
                notif.put("message", "You were not selected for " +
                        (eventName != null ? eventName : "this event") + ".");
                notif.put("timestamp", System.currentTimeMillis());
                notif.put("read", false);

                // send “not selected” notification + log
                sendNotifIfEnabled(cancelledId, notif);

                // remove any previous accept/decline invites for this event
                clearInvitationNotifications(cancelledId);
            }

            // automatically draw replacements for the same number we just cancelled
            drawReplacements(chosen.size());

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /** US 02.05.03 – Invite the next waiting entrant (FIFO).
     * Attempts to invite a specified number of users from the events waiting list
     * to fill open guests slots. Only Users that are not enrolled or previously invited
     * are not considered.
     *
     * @param count the maximum number of replacements to attempt to invite if {@code count <= 0}
     *              no action is taken
     * */
    private void drawReplacements(int count) {
        if (count <= 0) return;

        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> waiting  = (List<String>) (doc.get("waiting_list") != null ? doc.get("waiting_list") : doc.get("waitingList"));
            if (waiting == null) waiting = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<String> enrolled = (List<String>) (doc.get("enrolled_list") != null ? doc.get("enrolled_list") : doc.get("enrolledList"));
            if (enrolled == null) enrolled = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<String> invited  = (List<String>) (doc.get("invited_list") != null ? doc.get("invited_list") : doc.get("invitedList"));
            if (invited == null) invited = new ArrayList<>();



            String capStr = doc.getString("limitGuests");
            int capacity = 0;
            try {
                capacity = Integer.parseInt(capStr);
            } catch (Exception ignored) {}

            int open = Math.max(0, capacity - enrolled.size() - invited.size());
            if (open <= 0) {
                Toast.makeText(this, "No open slots to fill.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Choose up to min(open, count) FIFO from waiting, skipping already invited/enrolled
            List<String> chosen = new ArrayList<>();
            for (String id : waiting) {
                if (!invited.contains(id) && !enrolled.contains(id)) {
                    chosen.add(id);
                    if (chosen.size() == Math.min(open, count)) break;
                }
            }
            if (chosen.isEmpty()) {
                Toast.makeText(this, "No eligible replacements found.", Toast.LENGTH_SHORT).show();
                return;
            }

            eventRef.update(
                    "waiting_list", FieldValue.arrayRemove(chosen.toArray()),
                    "invited_list", FieldValue.arrayUnion(chosen.toArray())
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Invited " + chosen.size() + " replacement(s).", Toast.LENGTH_SHORT).show();

                // notify each replacement that they have been invited (US 01.05.01)
                String localEventName = doc.getString("eventName");
                String safeName = (localEventName != null && !localEventName.isEmpty())
                        ? localEventName
                        : "this event";

                for (String invitedId : chosen) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("eventId", eventId);
                    notif.put("eventName", localEventName != null ? localEventName : "Event");
                    notif.put("type", "INVITED");
                    notif.put("message", "You were selected for " + safeName + ". Tap the event to accept or decline.");
                    notif.put("timestamp", System.currentTimeMillis());
                    notif.put("read", false);

                    sendNotifIfEnabled(invitedId, notif);
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to invite replacements: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );


        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show()
        );
    }

    private void sendNotifIfEnabled(String targetId, Map<String, Object> notif) {
        db.collection("Profiles").document(targetId).get()
                .addOnSuccessListener(doc -> {
                    Boolean enabled = doc.getBoolean("notificationsEnabled");

                    if (enabled == null || enabled) {

                        // =============== 1) Send the notification ===============
                        db.collection("Profiles")
                                .document(targetId)
                                .collection("notifications")
                                .add(notif);

                        // =============== 2) Log it for admins ===================
                        Map<String, Object> log = new HashMap<>();
                        log.put("senderId", organizerDeviceId);
                        log.put("senderRole", "ORGANIZER");
                        log.put("recipientId", targetId);
                        log.put("eventId", notif.get("eventId"));
                        log.put("eventName", notif.get("eventName"));
                        log.put("type", notif.get("type")); // e.g. "NOT_SELECTED"
                        log.put("message", notif.get("message"));
                        log.put("timestamp", System.currentTimeMillis());

                        db.collection("NotificationLogs").add(log);
                    }
                });
    }

    private void clearInvitationNotifications(String targetId) {
        db.collection("Profiles")
                .document(targetId)
                .collection("notifications")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String type = doc.getString("type");
                        // delete only the “accept/decline” style invites
                        if ("INVITED".equals(type) || "INVITED_REPLACEMENT".equals(type)) {
                            doc.getReference().delete();
                        }
                    }
                });
    }

}

