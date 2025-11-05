// AdminImagesActivity
// This activity lists all events that have images uploaded.
// Admin can browse, search by event name, and open an event to manage images (placeholder toast for now).

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class defines the admin images screen.
 * It lets administrators view events that have uploaded images.
 */
public class AdminImagesActivity extends AppCompatActivity {

    private EditText etSearchImageEvent;
    private ListView listViewImageEvents;
    private AdminImagesListAdapter adapter;
    private List<EventModel> eventList = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    /**
     * Called when the activity is created.
     * Sets up the list, search bar, and loads events with images.
     * @param savedInstanceState The saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_images_activity);

        Button btnBack = findViewById(R.id.btnBackImages);
        btnBack.setOnClickListener(v -> finish());

        etSearchImageEvent = findViewById(R.id.etSearchImageEvent);
        listViewImageEvents = findViewById(R.id.listViewImageEvents);

        adapter = new AdminImagesListAdapter(this, eventList);
        listViewImageEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        etSearchImageEvent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadEventsWithImages();
    }

    /**
     * Loads all events that have an image URL from Firestore.
     */
    private void loadEventsWithImages() {
        eventsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) return;

            eventList.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots) {
                    EventModel event = doc.toObject(EventModel.class);
                    if (event != null && event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        event.setEventId(doc.getId());
                        eventList.add(event);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Filters the event list by name.
     * @param query The text entered in the search bar
     */
    private void filterEvents(String query) {
        List<EventModel> filtered = new ArrayList<>();
        for (EventModel event : eventList) {
            if (event.getEventName() != null &&
                    event.getEventName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(event);
            }
        }

        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    /**
     * Opens the image detail screen for the selected event.
     * @param event The selected event
     */
    public void openEventImageDetail(EventModel event) {
        Intent intent = new Intent(this, EventImageDetailActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("eventName", event.getEventName());
        intent.putExtra("organizerEmail", event.getOrganizerEmail());
        intent.putExtra("imageUrl", event.getImageUrl());
        startActivity(intent);
    }
}
