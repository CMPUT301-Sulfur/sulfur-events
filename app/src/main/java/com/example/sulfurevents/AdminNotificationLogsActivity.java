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
import java.util.List;

public class AdminNotificationLogsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private TextView tvEmpty;
    private AdminNotificationLogsAdapter adapter;
    private final List<NotificationLogItem> logs = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs_activity);

        rvLogs = findViewById(R.id.rvNotificationLogs);
        tvEmpty = findViewById(R.id.tvEmptyLogs);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        adapter = new AdminNotificationLogsAdapter(logs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(adapter);

        listenForLogs();
    }

    private void listenForLogs() {
        db.collection("NotificationLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    logs.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NotificationLogItem item = doc.toObject(NotificationLogItem.class);
                            if (item != null) logs.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}