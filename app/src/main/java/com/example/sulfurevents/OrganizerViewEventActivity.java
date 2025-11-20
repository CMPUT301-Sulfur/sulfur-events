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


// Java docs for part 3 finished 
/**
 * Displays detailed information about a specific event.
 * Includes a back button to return to the "Your Events" list.
 */
public class OrganizerViewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvDescription, tvStartDate, tvEndDate, tvLocation, tvCapacity, tvEmail;
    private ImageButton backButton;


    /**
     * Displays the details of a selected event and provides navigation
     * to view waitlisted, enrolled, and invited user lists.
     *
     * @param savedInstanceState Previous activity state if being restored.
     *
     * Steps:
     * <ul>
     *   <li>Retrieves the event ID passed from the previous screen</li>
     *   <li>Initializes and binds UI components</li>
     *   <li>Loads event details from Firestore and displays them</li>
     *   <li>Configures navigation to Waitlist, Enrolled, and Invited screens</li>
     *   <li>Back button returns the user to the Organizer home screen</li>
     * </ul>
     */
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

        //  Back button logic
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

        // Export Final list in CSV format file
        Button btnExportFinalListCSV = findViewById(R.id.btnExportFinalListCSV);
        btnExportFinalListCSV.setOnClickListener(v ->{
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerExportFinalListCSV.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

    }


    /**
     * Populates the event details screen using data from the retrieved Firestore document.
     *
     * @param doc The Firestore document containing event data.
     *
     * If the document does not exist, the activity closes. Otherwise, the event fields
     * are mapped and displayed in the corresponding TextViews.
     */
    private void populateEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        OrganizerEvent event = doc.toObject(OrganizerEvent.class);
        if (event == null) return;

        // fill text views with event data
        tvEventName.setText(event.getEventName());
        tvDescription.setText(event.getDescription());
        tvStartDate.setText("Start Date: " + event.getStartDate());
        tvEndDate.setText("End Date: " + event.getEndDate());
        tvLocation.setText("Location: " + event.getLocation());
        tvCapacity.setText("Capacity: " + event.getLimitGuests());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());
    }
}
