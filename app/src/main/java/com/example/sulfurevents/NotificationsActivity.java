package com.example.sulfurevents; // use your actual package

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

import com.example.sulfurevents.EventDetailsActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.NotificationActionListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private String deviceId;
    private NotificationsAdapter adapter;
    private List<NotificationItem> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        adapter = new NotificationsAdapter(notifications, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        listenForNotifications();
    }

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
                    notifications.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NotificationItem item = NotificationItem.fromDoc(doc);
                            notifications.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ====== Callbacks from adapter ======
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

    @Override
    public void onDecline(NotificationItem item) {
        db.collection("Events").document(item.eventId)
                .update(
                        "invited_list", FieldValue.arrayRemove(deviceId),
                        "cancelled_list", FieldValue.arrayUnion(deviceId)
                )
                .addOnSuccessListener(aVoid -> {
                    markNotificationRead(item);

                    // ✅ Popup to confirm decline
                    new AlertDialog.Builder(NotificationsActivity.this)
                            .setTitle("Invitation declined")
                            .setMessage("You declined the invitation" +
                                    (item.eventName != null ? " for " + item.eventName : "") + ".")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();

                    // ✅ Immediately draw a replacement and notify the next user
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

    @Override
    public void onViewEvent(NotificationItem item) {
        if (item.eventId == null || item.eventId.isEmpty()) {
            Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, EventDetailsActivity.class);
        i.putExtra("EVENT_ID", item.eventId);
        startActivity(i);
        // optional: mark as read
        markNotificationRead(item);
    }
    private void showResultDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
}
    private void markNotificationRead(NotificationItem item) {
        if (item.docId == null) return;
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .document(item.docId)
                .update("read", true);
    }

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
