package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminEntrantNotificationsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private TextView tvEmpty, tvHeader;
    private AdminNotificationLogsAdapter adapter;
    // After
    private final ArrayList<NotificationLogItem> logs = new ArrayList<>();
    private FirebaseFirestore db;

    private String entrantId;
    private String entrantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs_activity);

        entrantId = getIntent().getStringExtra("entrantId");
        entrantName = getIntent().getStringExtra("entrantName");

        db = FirebaseFirestore.getInstance();

        rvLogs = findViewById(R.id.rvNotificationLogs);
        tvEmpty = findViewById(R.id.tvEmptyLogs);
        tvHeader = findViewById(R.id.tvTitle);

        tvHeader.setText("Notifications for " + entrantName);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        adapter = new AdminNotificationLogsAdapter(logs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        db.collection("Profiles")
                .document(entrantId)
                .collection("notifications")
                .orderBy("timestamp")
                .addSnapshotListener((snap, error) -> {
                    logs.clear();

                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            NotificationLogItem item = doc.toObject(NotificationLogItem.class);
                            logs.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(logs.isEmpty() ? TextView.VISIBLE : TextView.GONE);
                });
    }
}
