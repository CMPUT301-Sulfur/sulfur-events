package com.example.sulfurevents;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;


public class EntrantActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private String deviceID;


    private Button viewGuidelines;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView eventLabel;
    private BottomNavigationView bottomNavigationView;


    private EventAdapter eventAdapter;
    private List<EventModel> eventList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.entrant_activity);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.waiting_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Initialize views
        viewGuidelines = findViewById(R.id.btn_lottery_guidelines);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.events_empty);
        eventLabel = findViewById(R.id.event_label);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        // Setup RecyclerView
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);


        // Setup Bottom Navigation
        BottomNavigationHelper.setupBottomNavigation(bottomNavigationView, this);
        bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);


        // Button listener
        viewGuidelines.setOnClickListener(v -> {
            startActivity(new Intent(EntrantActivity.this, LotteryGuidelinesActivity.class));
        });

        // Load available events
        loadJoinableEvents();
    }


    private void loadJoinableEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        eventLabel.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);


        db.collection("Events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    eventList.clear();


                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setText("No joinable events right now.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        eventLabel.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);


                        for (QueryDocumentSnapshot document : querySnapshot) {
                            EventModel event = document.toObject(EventModel.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading events: " + e.getMessage());
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
