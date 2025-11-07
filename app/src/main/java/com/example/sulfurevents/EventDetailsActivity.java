package com.example.sulfurevents;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays full details for a single event and lets an entrant:
 * <ul>
 *   <li>Join or leave the waiting list (capacity/closure aware)</li>
 *   <li>Accept or decline an invitation (moves between Firestore arrays)</li>
 * </ul>
 *
 * State is derived from the event document in Firestore:
 * <ul>
 *   <li><b>waiting_list</b> — device IDs waiting</li>
 *   <li><b>invited_list</b> — device IDs invited (pending)</li>
 *   <li><b>enrolled_list</b> — device IDs enrolled (accepted)</li>
 *   <li><b>finalized</b> — boolean: registration closed</li>
 *   <li><b>capacity</b> or <b>limitGuests/maxCapacity</b> — max seats</li>
 * </ul>
 *
 * Notes:
 * <ul>
 *   <li>Joining is blocked if the event is finalized or effectively full
 *       (capacity - invited - enrolled &lt;= 0).</li>
 *   <li>Accepting moves invited → enrolled (transactional, capacity-checked) and
 *       auto-finalizes if enrollment reaches capacity, also clearing waiting_list.</li>
 *   <li>Declining removes the device from invited_list (organizer draws replacements manually).</li>
 * </ul>
 */
public class EventDetailsActivity extends AppCompatActivity {

    // Firestore & ids
    private FirebaseFirestore db;
    private String deviceID;
    private String eventId;

    // UI
    private TextView eventNameText, descriptionText, organizerText, totalEntrantsText;
    private Button joinLeaveButton;
    private Button acceptInviteButton;
    private Button declineInviteButton;
    private ProgressBar progressBar;
    private ImageButton backButton;

    // State flags (derived from Firestore)
    private boolean isOnWaitingList = false;
    private boolean isInvited = false;
    private boolean isEnrolled = false;
    private boolean isFinalized = false;

    /**
     * Standard Activity entry point. Binds views, wires handlers, and loads state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_activity);

        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Intent extras provided by the caller
        eventId = getIntent().getStringExtra("eventId");
        String eventName   = getIntent().getStringExtra("eventName");
        String description = getIntent().getStringExtra("description");
        String organizer   = getIntent().getStringExtra("organizerEmail");

        // Bind views
        eventNameText       = findViewById(R.id.event_name_detail);
        descriptionText     = findViewById(R.id.event_description);
        organizerText       = findViewById(R.id.event_organizer);
        totalEntrantsText   = findViewById(R.id.total_entrants);
        joinLeaveButton     = findViewById(R.id.join_leave_button);
        acceptInviteButton  = findViewById(R.id.accept_invite_button);
        declineInviteButton = findViewById(R.id.decline_invite_button);
        progressBar         = findViewById(R.id.progressBar);
        backButton          = findViewById(R.id.back_button_details);

        // Static labels
        eventNameText.setText(eventName);
        descriptionText.setText(description != null ? description : "No description available");
        organizerText.setText("Organizer: " + (organizer != null ? organizer : "Unknown"));

        // Back
        backButton.setOnClickListener(v -> finish());

        // Join or leave the waiting list based on current state
        joinLeaveButton.setOnClickListener(v -> {
            if (isOnWaitingList) {
                leaveWaitingList();
            } else {
                tryJoinWaitingList();
            }
        });

        // Handle invite responses
        acceptInviteButton.setOnClickListener(v -> acceptInvitation());
        declineInviteButton.setOnClickListener(v -> declineInvitation());

        // Initial snapshot of state
        loadEventState();
    }

    /**
     * Refresh state when returning to the screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadEventState();
    }

    /**
     * Loads the event document and derives UI flags (waiting/invited/enrolled/finalized).
     * Also computes effective closure if enrolled >= capacity.
     */
    private void loadEventState() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Tolerant list reads (snake/camel)
                    List<String> waiting  = getStringList(documentSnapshot, "waiting_list", "waitingList");
                    List<String> invited  = getStringList(documentSnapshot, "invited_list", "invitedList");
                    List<String> enrolled = getStringList(documentSnapshot, "enrolled_list", "enrolledList");

                    if (waiting  == null) waiting  = new ArrayList<>();
                    if (invited  == null) invited  = new ArrayList<>();
                    if (enrolled == null) enrolled = new ArrayList<>();

                    // Derive current user's state
                    isOnWaitingList = waiting.contains(deviceID);
                    isInvited       = invited.contains(deviceID);
                    isEnrolled      = enrolled.contains(deviceID);

                    // Honor explicit finalized flag if present
                    isFinalized = Boolean.TRUE.equals(documentSnapshot.getBoolean("finalized"));

                    // If enrolled >= capacity, treat as finalized/closed for UI
                    int cap = getCapacity(documentSnapshot);
                    if (cap > 0 && enrolled.size() >= cap) {
                        isFinalized = true;
                    }

                    // Display total people in waiting list
                    totalEntrantsText.setText("Total Entrants: " + waiting.size());

                    updateButtonState();
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Applies the derived flags to the UI: which buttons show, text, and enabled state.
     */
    private void updateButtonState() {
        // Default: show join/leave
        joinLeaveButton.setVisibility(View.VISIBLE);
        acceptInviteButton.setVisibility(View.GONE);
        declineInviteButton.setVisibility(View.GONE);

        if (isInvited && !isEnrolled) {
            // If invited (pending), show Accept/Decline instead of Join/Leave
            joinLeaveButton.setVisibility(View.GONE);
            acceptInviteButton.setVisibility(View.VISIBLE);
            declineInviteButton.setVisibility(View.VISIBLE);
            return;
        }

        if (isEnrolled) {
            // Enrolled → lock the button
            joinLeaveButton.setText("You are enrolled");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_green_light)
            );
            return;
        }

        if (isFinalized) {
            // Closed → no more actions
            joinLeaveButton.setText("Registration closed");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.darker_gray)
            );
            return;
        }

        // Waiting vs not waiting
        if (isOnWaitingList) {
            joinLeaveButton.setText("Leave Waiting List");
            joinLeaveButton.setEnabled(true);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_red_light)
            );
        } else {
            joinLeaveButton.setText("Join Waiting List");
            joinLeaveButton.setEnabled(true);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_green_light)
            );
        }
    }

    /**
     * Attempts to join the waiting list, guarding against:
     * <ul>
     *   <li>finalized/closed events</li>
     *   <li>full capacity (capacity - invited - enrolled &lt;= 0)</li>
     *   <li>already enrolled, invited, or waiting</li>
     * </ul>
     */
    private void tryJoinWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        final DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get().addOnSuccessListener(doc -> {
            progressBar.setVisibility(View.GONE);
            joinLeaveButton.setEnabled(true);

            if (!doc.exists()) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean finalized = Boolean.TRUE.equals(doc.getBoolean("finalized"));

            List<String> waiting  = getStringList(doc, "waiting_list", "waitingList");
            List<String> invited  = getStringList(doc, "invited_list", "invitedList");
            List<String> enrolled = getStringList(doc, "enrolled_list", "enrolledList");

            if (waiting  == null) waiting  = new ArrayList<>();
            if (invited  == null) invited  = new ArrayList<>();
            if (enrolled == null) enrolled = new ArrayList<>();

            // Capacity-aware closure even if "finalized" is not set
            int capacity = getCapacity(doc);
            boolean finalFull = (capacity > 0 && enrolled.size() >= capacity);
            if (finalized || finalFull) {
                Toast.makeText(this, "Registration closed.", Toast.LENGTH_SHORT).show();
                return;
            }


            // Standard guards against duplicates/conflicts
            if (enrolled.contains(deviceID)) {
                Toast.makeText(this, "You are already registered.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (invited.contains(deviceID)) {
                Toast.makeText(this, "You’ve been invited—accept or decline first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (waiting.contains(deviceID)) {
                Toast.makeText(this, "You’re already on the waiting list.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Join the waiting list
            eventRef.update("waiting_list", FieldValue.arrayUnion(deviceID))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Joined waiting list.", Toast.LENGTH_SHORT).show();
                        loadEventState();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            joinLeaveButton.setEnabled(true);
            Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * Removes the current device from the waiting list.
     */
    private void leaveWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayRemove(deviceID))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully left waiting list", Toast.LENGTH_SHORT).show();
                    loadEventState();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Accept invitation:
     * <ul>
     *   <li>Transactional move: invited → enrolled</li>
     *   <li>Removes from waiting list if present</li>
     *   <li>Capacity-aware (fails if full)</li>
     *   <li>Auto-finalizes when filling last seat and clears waiting_list</li>
     * </ul>
     */
    private void acceptInvitation() {
        progressBar.setVisibility(View.VISIBLE);

        final DocumentReference eventRef = db.collection("Events").document(eventId);
        db.runTransaction(tr -> {
            DocumentSnapshot doc = tr.get(eventRef);

            List<String> invited  = getStringList(doc, "invited_list", "invitedList");
            List<String> enrolled = getStringList(doc, "enrolled_list", "enrolledList");
            List<String> waiting  = getStringList(doc, "waiting_list", "waitingList");
            if (invited  == null) invited  = new ArrayList<>();
            if (enrolled == null) enrolled = new ArrayList<>();
            if (waiting  == null) waiting  = new ArrayList<>();

            int capacity = getCapacity(doc);

            if (!invited.contains(deviceID)) {
                throw new IllegalStateException("Invitation no longer valid.");
            }
            if (enrolled.contains(deviceID)) {
                return null; // idempotent: already enrolled
            }

            if (capacity > 0 && enrolled.size() >= capacity) {
                throw new IllegalStateException("Event is full.");
            }

            // Move invited -> enrolled, and clean up waiting
            invited.remove(deviceID);
            waiting.remove(deviceID);
            enrolled.add(deviceID);

            Map<String, Object> updates = new HashMap<>();
            updates.put("invited_list", invited);
            updates.put("waiting_list", waiting);
            updates.put("enrolled_list", enrolled);

            // Auto-finalize when capacity reached; also hard-lock waiting list
            if (enrolled.size() >= capacity && capacity > 0) {
                updates.put("finalized", true);
                updates.put("waiting_list", new ArrayList<String>());
            }

            tr.update(eventRef, updates);
            return null;
        }).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Registered successfully.", Toast.LENGTH_SHORT).show();
            loadEventState();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            loadEventState();
        });
    }

    /**
     * Decline invitation: silently removes this device from invited_list.
     * Organizer can manually draw replacements; no auto-draw here.
     */
    private void declineInvitation() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Events").document(eventId)
                .update("invited_list", FieldValue.arrayRemove(deviceID))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                    loadEventState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Couldn't decline: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ===== Helpers =====

    /**
     * Tolerant getter for a list field that may exist under multiple keys.
     *
     * @param doc  Firestore document
     * @param keys Candidate field names (first present wins)
     * @return List of strings or null if none found / type mismatch
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
     * Reads capacity from either a numeric "capacity" or string "limitGuests"/"maxCapacity".
     *
     * @param doc Firestore document snapshot
     * @return capacity as int, or 0 if absent/invalid
     */
    private int getCapacity(DocumentSnapshot doc) {
        Number capNum = doc.getLong("capacity");
        if (capNum != null) return capNum.intValue();
        String cap = doc.getString("limitGuests");
        if (cap == null) cap = doc.getString("maxCapacity");
        try { return Integer.parseInt(cap); } catch (Exception ignored) { return 0; }
    }
}

