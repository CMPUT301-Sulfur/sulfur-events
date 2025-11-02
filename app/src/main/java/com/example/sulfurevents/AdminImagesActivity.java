// AdminImagesActivity
// This activity lists all events that have images uploaded.
// Admin can browse, search by event name, and open an event to manage images (placeholder toast for now).

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class AdminImagesActivity extends AppCompatActivity {

    private Button btnBack;
    private EditText etSearchImageEvent;
    private ListView listViewImageEvents;

    private AdminImagesListAdapter adapter;
    private List<ImageEventModel> eventList = new ArrayList<>();
    private List<ImageEventModel> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_images_activity);

        btnBack = findViewById(R.id.btnBackImages);
        etSearchImageEvent = findViewById(R.id.etSearchImageEvent);
        listViewImageEvents = findViewById(R.id.listViewImageEvents);

        btnBack.setOnClickListener(v -> finish());

        // Placeholder events
        eventList.add(new ImageEventModel("Music Night", "organizer1@email.com", "Active", R.drawable.ic_launcher_foreground));
        eventList.add(new ImageEventModel("Art Expo", "artist@email.com", "Expired", R.drawable.ic_launcher_foreground));
        eventList.add(new ImageEventModel("Tech Fair", "tech@email.com", "Active", R.drawable.ic_launcher_foreground));
        eventList.add(new ImageEventModel("Food Carnival", "chef@email.com", "Expired", R.drawable.ic_launcher_foreground));

        filteredList.addAll(eventList);

        adapter = new AdminImagesListAdapter(this, filteredList);
        listViewImageEvents.setAdapter(adapter);

        // search functionality
        etSearchImageEvent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterEvents(String query) {
        filteredList.clear();
        for (ImageEventModel event : eventList) {
            if (event.getEventName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(event);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
