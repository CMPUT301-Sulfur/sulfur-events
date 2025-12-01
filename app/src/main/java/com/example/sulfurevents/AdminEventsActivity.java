// AdminEventsActivity
// This activity loads all events from Firebase Database.
// The admin can view event details and delete events.

package com.example.sulfurevents;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * AdminEventsActivity
 * This screen allows administrators to view and delete events stored in Firestore.
 */
public class AdminEventsActivity extends AppCompatActivity {

    private ListView listViewEvents;
    private ArrayList<EventModel> eventList;
    private AdminEventsListAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    /**
     * Called when the activity is created.
     * Initializes UI components and loads events.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events_activity);

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBackEvents);
        btnBack.setOnClickListener(v -> finish());

        // Set up list + adapter
        listViewEvents = findViewById(R.id.listViewEvents);
        eventList = new ArrayList<>();
        adapter = new AdminEventsListAdapter(this, eventList);
        listViewEvents.setAdapter(adapter);

        // Firestore initialization
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        loadEventsFromFirestore();
    }

    /**
     * Loads all events from Firestore and listens for updates in real time.
     */
    private void loadEventsFromFirestore() {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                if (snapshots == null) return;

                eventList.clear();

                for (DocumentSnapshot doc : snapshots) {
                    EventModel event = doc.toObject(EventModel.class);
                    if (event != null) {
                        event.setEventId(doc.getId());
                        eventList.add(event);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Deletes an event from Firestore and shows a popup instead of a Toast.
     * @param eventId ID of the event to delete
     */
    public void deleteEvent(String eventId) {
        eventsRef.document(eventId).delete()
                .addOnSuccessListener(aVoid -> showPopup("Event deleted successfully."))
                .addOnFailureListener(e -> showPopup("Failed to delete event."));
    }

    /**
     * Shows a simple popup dialog with a message.
     * @param message Text to display
     */
    private void showPopup(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Action")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
