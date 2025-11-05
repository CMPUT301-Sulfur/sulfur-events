package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantViewEventsActivity extends AppCompatActivity
        implements EntrantEventsAdapter.OnJoinClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EntrantEventsAdapter adapter;
    private final List<EventModel> joinableEvents = new ArrayList<>();

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    // later you can swap this for deviceId or FirebaseAuth uid
    private static final String CURRENT_USER_ID = "demo-user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_view_events);

        recyclerView = findViewById(R.id.recycler_joinable_events);
        progressBar = findViewById(R.id.progress_joinable_events);

        // NEW: lottery guidelines button (make sure it's in the layout)
        View btnGuidelines = findViewById(R.id.btn_lottery_guidelines);
        if (btnGuidelines != null) {
            btnGuidelines.setOnClickListener(v -> {
                Intent intent = new Intent(EntrantViewEventsActivity.this, LotteryGuidelinesActivity.class);
                startActivity(intent);
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntrantEventsAdapter(joinableEvents, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        loadJoinableEvents();
    }

    private void loadJoinableEvents() {
        progressBar.setVisibility(View.VISIBLE);

        // only show events that are "open"
        eventsRef.whereEqualTo("status", "open")
                .addSnapshotListener((@Nullable QuerySnapshot value,
                                      @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    joinableEvents.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            EventModel event = doc.toObject(EventModel.class);
                            // keep Firestore doc id
                            event.setEventId(doc.getId());

                            // check if THIS user is in this event's waitingList
                            DocumentReference waitingDoc = db.collection("events")
                                    .document(event.getEventId())
                                    .collection("waitingList")
                                    .document(CURRENT_USER_ID);

                            waitingDoc.get().addOnSuccessListener(waitingSnapshot -> {
                                boolean joined = waitingSnapshot.exists();
                                event.setJoinedByCurrentUser(joined);
                                adapter.notifyDataSetChanged();
                            });

                            joinableEvents.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onJoinClicked(EventModel event) {
        DocumentReference waitingRef = db.collection("events")
                .document(event.getEventId())
                .collection("waitingList")
                .document(CURRENT_USER_ID);

        String ts = String.valueOf(System.currentTimeMillis());
        WaitingListEntry entry = new WaitingListEntry(CURRENT_USER_ID, ts);

        waitingRef.set(entry)
                .addOnSuccessListener(aVoid -> {
                    event.setJoinedByCurrentUser(true);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onLeaveClicked(EventModel event) {
        DocumentReference waitingRef = db.collection("events")
                .document(event.getEventId())
                .collection("waitingList")
                .document(CURRENT_USER_ID);

        waitingRef.delete()
                .addOnSuccessListener(aVoid -> {
                    event.setJoinedByCurrentUser(false);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
