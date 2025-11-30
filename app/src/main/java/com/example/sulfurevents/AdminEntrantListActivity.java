package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminEntrantListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private ArrayList<ProfileModel> userList = new ArrayList<>();
    private AdminEntrantListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_entrant_list);

        ImageButton backBtn = findViewById(R.id.btnBackEntrants);
        backBtn.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerEntrants);
        progressBar = findViewById(R.id.progressEntrants);

        db = FirebaseFirestore.getInstance();

        adapter = new AdminEntrantListAdapter(userList, user -> {
            Intent intent = new Intent(AdminEntrantListActivity.this, AdminNotificationLogsActivity.class);
            intent.putExtra("deviceId", user.getDeviceId());
            intent.putExtra("name", user.getName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadEntrants();
    }

    private void loadEntrants() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Profiles")
                .get()
                .addOnSuccessListener(query -> {
                    progressBar.setVisibility(View.GONE);
                    userList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        ProfileModel user = doc.toObject(ProfileModel.class);
                        user.setProfileId(doc.getId());
                        userList.add(user);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
