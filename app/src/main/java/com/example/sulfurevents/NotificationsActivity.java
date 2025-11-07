package com.example.sulfurevents; // use your actual package

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sulfurevents.EventDetailsActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
                        "invited_list", com.google.firebase.firestore.FieldValue.arrayRemove(deviceId),
                        "cancelled_list", com.google.firebase.firestore.FieldValue.arrayUnion(deviceId)
                )
                .addOnSuccessListener(aVoid -> {
                    markNotificationRead(item);
                    // ✅ this is the popup
                    new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                            .setTitle("Invitation declined")
                            .setMessage("You declined the invitation" +
                                    (item.eventName != null ? " for " + item.eventName : "") + ".")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                })
                .addOnFailureListener(e ->
                        new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
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
}
