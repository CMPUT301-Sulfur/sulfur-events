package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.ArrayList;

/**
 * This activity displays all notification logs for a selected user.
 * Administrators can review messages such as event updates, lottery results,
 * and other system-generated notifications linked to that user.
 */
public class AdminNotificationLogsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private TextView tvEmpty, tvTitle;
    private AdminNotificationLogsAdapter adapter;
    private final ArrayList<NotificationLogItem> logs = new ArrayList<>();
    private FirebaseFirestore db;

    private String deviceId;
    private String userName;

    /**
     * Called when the activity is created.
     * Initializes UI elements, configures the RecyclerView,
     * retrieves intent data, and loads notification logs.
     *
     * @param savedInstanceState Previously saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs_activity);

        rvLogs = findViewById(R.id.rvNotificationLogs);
        tvEmpty = findViewById(R.id.tvEmptyLogs);
        tvTitle = findViewById(R.id.tvTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        deviceId = getIntent().getStringExtra("deviceId");
        userName = getIntent().getStringExtra("name");
        tvTitle.setText("Notifications for " + userName);

        adapter = new AdminNotificationLogsAdapter(logs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(adapter);

        loadLogs();
    }

    /**
     * Loads the notification logs for the selected user from Firestore.
     * Updates the RecyclerView and displays an empty-state message if needed.
     */
    private void loadLogs() {
        db.collection("Profiles")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {

                    logs.clear();

                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            NotificationLogItem item = doc.toObject(NotificationLogItem.class);
                            if (item != null) logs.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

}
