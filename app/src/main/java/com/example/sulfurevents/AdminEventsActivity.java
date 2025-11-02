// AdminEventsActivity
// This activity shows all events for the admin. It lets the admin browse events,
// search by event name, and (later) delete events from the database.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity {

    private Button btnBack;
    private EditText etSearchEvent;
    private ListView listViewEvents;

    // Our custom adapter (AdminEventsListAdapter)
    private AdminEventsListAdapter adapter;

    // Placeholder data lists
    private List<EventModel> eventList = new ArrayList<>();
    private List<EventModel> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events_activity);

        // UI elements
        btnBack = findViewById(R.id.btnBackEvents);
        etSearchEvent = findViewById(R.id.etSearchEvent);
        listViewEvents = findViewById(R.id.listViewEvents);

        // Back button
        btnBack.setOnClickListener(v -> finish()); // returns to Admin Dashboard

        // Placeholder event data
        eventList.add(new EventModel("Music Night", "organizer1@email.com", "Active"));
        eventList.add(new EventModel("Art Expo", "artist@email.com", "Expired"));
        eventList.add(new EventModel("Tech Fair", "techguy@email.com", "Active"));
        eventList.add(new EventModel("Food Carnival", "chef@email.com", "Expired"));


        // copy all items to filtered list initially
        filteredList.addAll(eventList);

        // set adapter
        adapter = new AdminEventsListAdapter(this, filteredList);
        listViewEvents.setAdapter(adapter);

        // search functionality
        etSearchEvent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // filters the event list based on search input
    private void filterEvents(String query) {
        filteredList.clear();
        for (EventModel event : eventList) {
            if (event.getEventName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(event);
            }
        }
        adapter.notifyDataSetChanged(); // refresh list view
    }
}
