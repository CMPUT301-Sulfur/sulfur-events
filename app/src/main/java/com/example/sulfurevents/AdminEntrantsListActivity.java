package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminEntrantsListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private AdminEntrantsListAdapter adapter;

    private final List<User> users = new ArrayList<>();
    private final List<String> userIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_entrants_list_activity);

        db = FirebaseFirestore.getInstance();

        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageButton back = findViewById(R.id.btnBack);

        back.setOnClickListener(v -> finish());

        adapter = new AdminEntrantsListAdapter(users, userIds, this::openEntrantNotifications);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        loadProfiles();
    }

    private void loadProfiles() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        users.clear();
        userIds.clear();

        db.collection("Profiles")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        User u = doc.toObject(User.class);
                        if (u.getName() == null || u.getName().isEmpty()) {
                            u.setName("(Unnamed)");
                        }
                        users.add(u);
                        userIds.add(doc.getId());
                    }

                    progressBar.setVisibility(View.GONE);
                    if (users.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No entrants found.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load entrants.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openEntrantNotifications(String userId, String name) {
        Intent intent = new Intent(this, AdminEntrantNotificationsActivity.class);
        intent.putExtra("entrantId", userId);
        intent.putExtra("entrantName", name);
        startActivity(intent);
    }
}
