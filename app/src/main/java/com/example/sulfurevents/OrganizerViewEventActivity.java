package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays detailed information about a specific event.
 * Includes a back button to return to the "Your Events" list.
 */
public class OrganizerViewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvDescription, tvStartDate, tvEndDate, tvLocation, tvCapacity, tvEmail;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_view_event_activity);

        db = FirebaseFirestore.getInstance();

        // get event id from intent
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // initialize UI
        backButton = findViewById(R.id.backButtonEventDetails);
        tvEventName = findViewById(R.id.tvEventName);
        tvDescription = findViewById(R.id.tvDescription);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvEmail = findViewById(R.id.tvEmail);

        // 🔹 Back button logic
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerViewEventActivity.this, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // load event details
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(this::populateEvent)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                    finish();
                });

        /**
         * Launches the OrganizerWaitlistActivity to display the current waiting list for this event.
         */
        Button btnViewWaitingList = findViewById(R.id.btnViewWaitingList);
        btnViewWaitingList.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerWaitlistActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        Button btnViewEnrolled = findViewById(R.id.btnViewEnrolled);
        btnViewEnrolled.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerEnrolledActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        Button btnViewInvited = findViewById(R.id.btnViewInvited);
        btnViewInvited.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerInvitedActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });




    }

    private void populateEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        OrganizerEvent event = doc.toObject(OrganizerEvent.class);
        if (event == null) return;

        // fill text views with event data
        tvEventName.setText(event.getEventTitle());
        tvDescription.setText(event.getDescription());
        tvStartDate.setText("Start Date: " + event.getStartDate());
        tvEndDate.setText("End Date: " + event.getEndDate());
        tvLocation.setText("Location: " + event.getLocation());
        tvCapacity.setText("Capacity: " + event.getLimitGuests());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());
    }
}
