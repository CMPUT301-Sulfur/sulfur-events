// EventImageDetailActivity
// This activity displays all images for a selected event.
// The admin can view and delete specific images from here.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class EventImageDetailActivity extends AppCompatActivity {

    private Button btnBack;
    private TextView tvEventTitle, tvEventInfo;
    private ListView listViewEventImages;

    private EventImageDetailAdapter adapter;
    private List<Integer> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_image_detail_activity);

        btnBack = findViewById(R.id.btnBackEventImages);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        listViewEventImages = findViewById(R.id.listViewEventImages);

        btnBack.setOnClickListener(v -> finish());

        // Get event info
        String eventName = getIntent().getStringExtra("eventName");
        String organizerEmail = getIntent().getStringExtra("organizerEmail");

        tvEventTitle.setText(eventName + " Images");
        tvEventInfo.setText("Organizer: " + organizerEmail);

        // placeholder image data
        imageList.add(R.drawable.ic_launcher_foreground);
        imageList.add(R.drawable.ic_launcher_foreground);
        imageList.add(R.drawable.ic_launcher_foreground);

        // setup adapter
        adapter = new EventImageDetailAdapter(this, imageList);
        listViewEventImages.setAdapter(adapter);
    }
}
