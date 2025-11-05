package com.example.sulfurevents;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantNotificationsActivity extends AppCompatActivity implements NotificationAdapter.NotificationActionListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications = new ArrayList<>();
    private FirebaseFirestore db;
    private String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_notifications);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        profileId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications, this);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        db.collection("Notifications")
                .whereEqualTo("toProfileId", profileId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : qs) {
                        NotificationModel n = doc.toObject(NotificationModel.class);
                        n.setId(doc.getId());
                        notifications.add(n);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAccept(@NonNull NotificationModel notification) {
        db.collection("Events").document(notification.getEventId())
                .collection("WaitingList").document(profileId)
                .update("status", "accepted")
                .addOnSuccessListener(unused -> {
                    db.collection("Notifications").document(notification.getId())
                            .update("handled", true);
                    Toast.makeText(this, "Invitation accepted", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
    }

    @Override
    public void onDecline(@NonNull NotificationModel notification) {
        db.collection("Events").document(notification.getEventId())
                .collection("WaitingList").document(profileId)
                .update("status", "declined")
                .addOnSuccessListener(unused -> {
                    db.collection("Notifications").document(notification.getId())
                            .update("handled", true);
                    Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
