package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EntrantEventsAdapter adapter;
    private List<EventModel> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_activity);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);  // add this in the layout

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntrantEventsAdapter(this, eventList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadJoinableEvents();
    }

    private void loadJoinableEvents() {
        progressBar.setVisibility(View.VISIBLE);

        // only pull events that are joinable
        db.collection("events")
                .whereEqualTo("status", "open")   // <-- match your actual status value
                .get()
                .addOnSuccessListener(query -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        EventModel event = doc.toObject(EventModel.class);
                        // set Firestore doc id into eventId so adapter can write in that event
                        event.setEventId(doc.getId());
                        eventList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (eventList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load events.");
                });
    }
}
