package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code NotificationsActivity} presents the list of system notifications that are
 * addressed to the currently identified entrant. This screen enables the entrant to
 * view lottery outcomes and to react to invitation notifications by accepting or
 * declining them.
 * <p>
 * This activity contributes directly to the implementation of the following user stories:
 * <ul>
 *     <li><b>US 01.04.01</b> – “As an entrant, I want to receive notification when I am chosen
 *     to participate from the waiting list (when I ‘win’ the lottery).”</li>
 *     <li><b>US 01.04.02</b> – “As an entrant, I want to receive notification when I am not
 *     chosen on the app (when I ‘lose’ the lottery).”</li>
 *     <li><b>US 01.05.02</b> – “As an entrant, I want to be able to accept the invitation to
 *     register/sign up when chosen to participate in an event.”</li>
 *     <li><b>US 01.05.03</b> – “As an entrant, I want to be able to decline an invitation when
 *     chosen to participate in an event.”</li>
 * </ul>
 * The activity retrieves notification documents from Firestore under
 * {@code Profiles/<deviceId>/notifications} and binds them to a {@link RecyclerView} through
 * {@link NotificationsAdapter}. For invitation-type notifications, the activity updates the
 * event document to move the entrant from {@code invited_list} to the appropriate list
 * ({@code enrolled_list} or {@code cancelled_list}), and may trigger a replacement draw.
 * <p>Author: sulfur (CMPUT 301-Part 3)</p>
 */
public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.NotificationActionListener {

    private RecyclerView rvNewNotifications, rvHistoryNotifications;
    private TextView tvEmptyNew, tvEmptyHistory, tvToggleHistory;

    private FirebaseFirestore db;
    private String deviceId;

    private NotificationsAdapter newAdapter, historyAdapter;
    private final List<NotificationItem> newNotifications = new ArrayList<>();
    private final List<NotificationItem> historyNotifications = new ArrayList<>();

    private boolean historyVisible = false;

    /**
     * Initializes the notifications screen, prepares the RecyclerView and starts
     * listening to Firestore for changes in the entrant's notification subcollection.
     * @param savedInstanceState previously saved state (unused here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // NEW view bindings (match the new XML ids)
        rvNewNotifications = findViewById(R.id.rvNewNotifications);
        rvHistoryNotifications = findViewById(R.id.rvHistoryNotifications);

        tvEmptyNew = findViewById(R.id.tvEmptyNew);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        tvToggleHistory = findViewById(R.id.tvToggleHistory);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        // NEW adapters + lists
        newAdapter = new NotificationsAdapter(newNotifications, this);
        historyAdapter = new NotificationsAdapter(historyNotifications, this);

        rvNewNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvHistoryNotifications.setLayoutManager(new LinearLayoutManager(this));

        rvNewNotifications.setAdapter(newAdapter);
        rvHistoryNotifications.setAdapter(historyAdapter);

        // Collapsible history toggle
        tvToggleHistory.setOnClickListener(v -> {
            historyVisible = !historyVisible;
            rvHistoryNotifications.setVisibility(historyVisible ? View.VISIBLE : View.GONE);

            // show empty history text only when history is open
            tvEmptyHistory.setVisibility(historyVisible && historyNotifications.isEmpty()
                    ? View.VISIBLE : View.GONE);

            tvToggleHistory.setText(historyVisible ? "Hide history" : "Show history");
        });

        listenForNotifications(); // same name, but updated body
    }

    /**
     * Lifecycle callback invoked when the notifications screen is no longer in the
     * foreground (e.g., user navigates away or the app is backgrounded).
     *
     * <p>To keep the UI tidy, this marks all non-invitation notifications as read so
     * that on the next visit they appear in the “history” section instead of the
     * primary list of new notifications. Invitation notifications remain unread
     * until the user explicitly accepts or declines them.</p>
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Mark non-invitation notifications as read when user leaves this screen
        markNonInvitationNotificationsAsRead();
    }

    /**
     * Marks all unread, non-invitation notifications for the current device as read.
     *
     * <p>This method queries {@code Profiles/{deviceId}/notifications} for documents
     * where {@code read == false} and sets {@code read = true} for every notification
     * whose {@code type} is not {@code "INVITED"}. This ensures that informational
     * and outcome messages (e.g., NOT_SELECTED) are moved to the history section,
     * while invitation messages remain highlighted until the entrant responds.</p>
     */
    private void markNonInvitationNotificationsAsRead() {
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String type = doc.getString("type");

                        // Only mark as read if it's NOT an invitation
                        if (!"INVITED".equalsIgnoreCase(type)) {
                            doc.getReference().update("read", true);
                        }
                    }
                });
    }    /**
     * Subscribes to Firestore updates on the current entrant's notification collection.
     * The list is kept in reverse chronological order (latest first). When no notifications
     * are present, an empty-state text view is shown.
     */
    private void listenForNotifications() {
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    newNotifications.clear();
                    historyNotifications.clear();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NotificationItem item = NotificationItem.fromDoc(doc);

                            if (item.read) {
                                historyNotifications.add(item);
                            } else {
                                newNotifications.add(item);
                            }
                        }
                    }

                    newAdapter.notifyDataSetChanged();
                    historyAdapter.notifyDataSetChanged();

                    tvEmptyNew.setVisibility(newNotifications.isEmpty() ? View.VISIBLE : View.GONE);

                    if (historyVisible) {
                        tvEmptyHistory.setVisibility(historyNotifications.isEmpty()
                                ? View.VISIBLE : View.GONE);
                    } else {
                        tvEmptyHistory.setVisibility(View.GONE);
                    }
                });
    }

    // ====== Callbacks from adapter ======

    /**
     * Handles the entrant's acceptance of an invitation notification.
     * This updates the corresponding event document so that the entrant is
     * removed from {@code invited_list} and added to {@code enrolled_list}.
     * A confirmation dialog is shown, and the method also evaluates whether the
     * event has become full so that remaining entrants can be informed they were
     * not selected.
     *
     * @param item the notification representing the invitation
     */
    @Override
    public void onAccept(NotificationItem item) {
        db.collection("Events").document(item.eventId)
                .update(
                        "invited_list", com.google.firebase.firestore.FieldValue.arrayRemove(deviceId),
                        "enrolled_list", com.google.firebase.firestore.FieldValue.arrayUnion(deviceId)
                )
                .addOnSuccessListener(aVoid -> {
                    markNotificationRead(item);
                    new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                            .setTitle("Invitation accepted")
                            .setMessage("You have been enrolled" +
                                    (item.eventName != null ? " for " + item.eventName : "") + ".")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    checkAndNotifyNotSelectedIfFull(item.eventId);
                })
                .addOnFailureListener(e ->
                        new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                                .setTitle("Error")
                                .setMessage("Couldn't accept: " + e.getMessage())
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show()
                );
    }

    /**
     * Handles the entrant's decline of an invitation notification.
     * This updates the event to move the entrant from {@code invited_list} to
     * {@code cancelled_list}, shows a confirmation dialog, and then attempts to
     * draw a replacement entrant from the waiting list.
     *
     * @param item the notification representing the invitation to decline
     */
    @Override
    public void onDecline(NotificationItem item) {
        db.collection("Events").document(item.eventId)
                .update(
                        "invited_list", FieldValue.arrayRemove(deviceId),
                        "cancelled_list", FieldValue.arrayUnion(deviceId)
                )
                .addOnSuccessListener(aVoid -> {
                    markNotificationRead(item);

                    new AlertDialog.Builder(NotificationsActivity.this)
                            .setTitle("Invitation declined")
                            .setMessage("You declined the invitation" +
                                    (item.eventName != null ? " for " + item.eventName : "") + ".")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();

                    drawReplacementAndNotify(item.eventId);
                })
                .addOnFailureListener(e ->
                        new AlertDialog.Builder(NotificationsActivity.this)
                                .setTitle("Error")
                                .setMessage("Couldn't decline: " + e.getMessage())
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show()
                );
    }

    /**
     * Handles the entrant's request to view details for the event related to
     * the selected notification. This starts {@link EventDetailsActivity} with
     * the event identifier passed as an extra. The notification is also marked read.
     *
     * @param item the notification whose event should be opened
     */
    @Override
    public void onViewEvent(NotificationItem item) {
        if (item.eventId == null || item.eventId.isEmpty()) {
            Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, EventDetailsActivity.class);
        i.putExtra("eventId", item.eventId);
        startActivity(i);
        markNotificationRead(item);
    }

    /**
     * Utility dialog method used to present immediate feedback to the entrant,
     * e.g., after accepting or declining an invitation.
     *
     * @param title   dialog title
     * @param message dialog message
     */
    private void showResultDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Marks the given notification as read in Firestore so that it no longer
     * appears as an unread item in the UI.
     *
     * @param item the notification item to mark as read
     */
    private void markNotificationRead(NotificationItem item) {
        if (item.docId == null) return;
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .document(item.docId)
                .update("read", true);
    }

    /**
     * Attempts to draw a replacement entrant from the event's waiting list after
     * a previously invited entrant declined. The drawn entrant is moved to
     * {@code invited_list} and receives an "INVITED" notification. If the waiting
     * list is empty, the method falls back to checking whether the event is full
     * and, if so, notifies the remaining waiting entrants that they were not selected.
     *
     * @param eventId identifier of the event for which a replacement should be drawn
     */
    private void drawReplacementAndNotify(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    List<String> waiting = (List<String>) doc.get("waiting_list");
                    String eventName = doc.getString("eventName");
                    if (eventName == null) eventName = "Event";

                    if (waiting == null || waiting.isEmpty()) {
                        // no one to invite → we can later check if event is full and send NOT_SELECTED
                        checkAndNotifyNotSelectedIfFull(doc, eventId, eventName);
                        return;
                    }

                    // take the first waiting user (you can randomize if you want)
                    String nextId = waiting.get(0);
                    checkAndNotifyNotSelectedIfFull(eventId);
                    String finalEventName = eventName;
                    db.collection("Events").document(eventId)
                            .update(
                                    "waiting_list", com.google.firebase.firestore.FieldValue.arrayRemove(nextId),
                                    "invited_list", com.google.firebase.firestore.FieldValue.arrayUnion(nextId)
                            )
                            .addOnSuccessListener(aVoid -> {
                                // send invite notification to that next user
                                Map<String, Object> notif = new HashMap<>();
                                notif.put("eventId", eventId);
                                notif.put("eventName", finalEventName);
                                notif.put("type", "INVITED");
                                notif.put("message", "You were selected for " + finalEventName + ". Tap to accept or decline.");
                                notif.put("timestamp", System.currentTimeMillis());
                                notif.put("read", false);

                                db.collection("Profiles")
                                        .document(nextId)
                                        .collection("notifications")
                                        .add(notif);
                            });
                });
    }

    /**
     * Variant of the “not selected” check that operates on an already-fetched
     * {@link DocumentSnapshot}. This is used in the branch where we already have
     * the event document (e.g., in {@link #drawReplacementAndNotify(String)}).
     *
     * @param eventDoc  the already-retrieved event document
     * @param eventId   identifier of the event
     * @param eventName human-readable name of the event
     */
    private void checkAndNotifyNotSelectedIfFull(DocumentSnapshot eventDoc,
                                                 String eventId,
                                                 String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get lists
        List<String> waiting = (List<String>) eventDoc.get("waiting_list");
        List<String> enrolled = (List<String>) eventDoc.get("enrolled_list");
        List<String> invited  = (List<String>) eventDoc.get("invited_list");

        if (waiting == null) waiting = new ArrayList<>();
        if (enrolled == null) enrolled = new ArrayList<>();
        if (invited == null) invited   = new ArrayList<>();

        // get capacity (change this key to match your Firestore field, e.g. "capacity" or "maxEntrants")
        Long capacityLong = eventDoc.getLong("capacity");
        if (capacityLong == null) {
            capacityLong = eventDoc.getLong("maxEntrants"); // fallback if you used another name
        }
        int capacity = capacityLong != null ? capacityLong.intValue() : -1;

        // ✅ check if enrollment deadline passed
        com.google.firebase.Timestamp deadline = eventDoc.getTimestamp("registrationEnd");
        boolean deadlinePassed = false;
        if (deadline != null) {
            deadlinePassed = deadline.toDate().getTime() <= System.currentTimeMillis();
        }

        // ✅ Condition: either event is full OR deadline has passed
        boolean isFull = capacity > 0 && enrolled.size() >= capacity;

        if (isFull || deadlinePassed) {
            // everyone still on waiting_list (and not enrolled/invited) gets NOT_SELECTED
            for (String deviceId : waiting) {
                if (enrolled.contains(deviceId)) continue;
                if (invited.contains(deviceId)) continue;

                Map<String, Object> notif = new HashMap<>();
                notif.put("eventId", eventId);
                notif.put("eventName", eventName);
                notif.put("type", "NOT_SELECTED");
                notif.put("message", "You were not selected for " + eventName + ".");
                notif.put("timestamp", System.currentTimeMillis());
                notif.put("read", false);

                db.collection("Profiles")
                        .document(deviceId)
                        .collection("notifications")
                        .add(notif);
            }

            // optional: clear waiting list since event is finished
            db.collection("Events").document(eventId)
                    .update("waiting_list", new ArrayList<String>());

            Log.d("NotificationsActivity", "Sent NOT_SELECTED notifications for " +
                    eventName + " (full=" + isFull + ", deadline=" + deadlinePassed + ")");
        }
    }

    /**
     * Convenience overload that fetches the event document first and then delegates
     * to {@link #checkAndNotifyNotSelectedIfFull(DocumentSnapshot, String, String)}.
     * This is used in places where we only have the eventId.
     *
     * @param eventId identifier of the event to evaluate for “not selected” notifications
     */
    private void checkAndNotifyNotSelectedIfFull(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) return;

                    // 1) name
                    String eventName = eventDoc.getString("eventName");
                    if (eventName == null) eventName = "Event";

                    // 2) lists (tolerant like the rest of your project)
                    @SuppressWarnings("unchecked")
                    List<String> waiting  = (List<String>) (eventDoc.get("waiting_list") != null
                            ? eventDoc.get("waiting_list")
                            : eventDoc.get("waitingList"));
                    if (waiting == null) waiting = new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<String> enrolled = (List<String>) (eventDoc.get("enrolled_list") != null
                            ? eventDoc.get("enrolled_list")
                            : eventDoc.get("enrolledList"));
                    if (enrolled == null) enrolled = new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<String> invited  = (List<String>) (eventDoc.get("invited_list") != null
                            ? eventDoc.get("invited_list")
                            : eventDoc.get("invitedList"));
                    if (invited == null) invited = new ArrayList<>();

                    // 3) capacity — your app uses "limitGuests" as STRING
                    String capStr = eventDoc.getString("limitGuests");
                    int capacity = 0;
                    try {
                        capacity = Integer.parseInt(capStr);
                    } catch (Exception ignored) {
                        // if it's missing or bad, we just don't send not-selected
                    }

                    if (capacity <= 0) {
                        // no capacity recorded → nothing to do
                        return;
                    }

                    // 4) in YOUR codebase an invited person also occupies a slot
                    int currentlyTaken = enrolled.size() + invited.size();
                    boolean isFull = currentlyTaken >= capacity;

                    if (!isFull) {
                        return; // still room, don't tell people "not selected" yet
                    }

                    // 5) event is full → tell everyone still waiting (and not already invited/enrolled)
                    for (String deviceId : waiting) {
                        if (enrolled.contains(deviceId)) continue;
                        if (invited.contains(deviceId)) continue;

                        Map<String, Object> notif = new HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("eventName", eventName);
                        notif.put("type", "NOT_SELECTED");
                        notif.put("message", "You were not selected for " + eventName + ".");
                        notif.put("timestamp", System.currentTimeMillis());
                        notif.put("read", false);

                        db.collection("Profiles")
                                .document(deviceId)
                                .collection("notifications")
                                .add(notif);
                    }

                    // optional: clear waiting list since no more room
                    db.collection("Events").document(eventId)
                            .update("waiting_list", new ArrayList<String>());
                });
    }
}