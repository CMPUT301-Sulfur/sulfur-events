// AdminEventsActivity
// This activity loads all events from Firebase Database.
// The admin can view event details and delete events.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class AdminEventsActivity extends AppCompatActivity {

    private ListView listViewEvents;
    private ArrayList<EventModel> eventList;
    private AdminEventsListAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events_activity);

        Button btnBack = findViewById(R.id.btnBackEvents);
        btnBack.setOnClickListener(v -> finish());

        listViewEvents = findViewById(R.id.listViewEvents);
        eventList = new ArrayList<>();
        adapter = new AdminEventsListAdapter(this, eventList);
        listViewEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        loadEventsFromFirestore();
    }

    private void loadEventsFromFirestore() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                eventList.clear();

                for (DocumentSnapshot doc : snapshots) {
                    EventModel event = doc.toObject(EventModel.class);
                    if (event != null) {
                        event.setEventId(doc.getId()); // store document ID
                        eventList.add(event);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    // Delete event by document ID
    public void deleteEvent(String eventId) {
        eventsRef.document(eventId).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show());
    }
}