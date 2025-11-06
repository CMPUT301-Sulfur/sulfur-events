package com.example.sulfurevents;


import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;


public class EventDetailsActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private String deviceID;
    private String eventId;


    private TextView eventNameText, descriptionText, organizerText, totalEntrantsText;
    private Button joinLeaveButton;
    private ProgressBar progressBar;
    private ImageButton backButton;


    private boolean isOnWaitingList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_activity);


        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Get intent extras
        eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");
        String description = getIntent().getStringExtra("description");
        String organizer = getIntent().getStringExtra("organizerEmail");


        // Initialize views
        eventNameText = findViewById(R.id.event_name_detail);
        descriptionText = findViewById(R.id.event_description);
        organizerText = findViewById(R.id.event_organizer);
        totalEntrantsText = findViewById(R.id.total_entrants);
        joinLeaveButton = findViewById(R.id.join_leave_button);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.back_button_details);


        // Set event details
        eventNameText.setText(eventName);
        descriptionText.setText(description != null ? description : "No description available");
        organizerText.setText("Organizer: " + (organizer != null ? organizer : "Unknown"));


        // Back button
        backButton.setOnClickListener(v -> finish());


        // Check if user is on waiting list and load entrant count
        checkWaitingListStatus();


        joinLeaveButton.setOnClickListener(v -> {
            if (isOnWaitingList) {
                leaveWaitingList();
            } else {
                joinWaitingList();
            }
        });
    }


    private void checkWaitingListStatus() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);


        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> waitingList = (List<String>) documentSnapshot.get("waiting_list");


                        if (waitingList != null) {
                            isOnWaitingList = waitingList.contains(deviceID);
                            updateButtonState();


                            // Display total entrants
                            totalEntrantsText.setText("Total Entrants: " + waitingList.size());
                        } else {
                            isOnWaitingList = false;
                            updateButtonState();
                            totalEntrantsText.setText("Total Entrants: 0");
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
                });
    }


    private void joinWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);


        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(aVoid -> {
                    isOnWaitingList = true;
                    updateButtonState();
                    Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
                    checkWaitingListStatus(); // Refresh count
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                });
    }


    private void leaveWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);


        db.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayRemove(deviceID))
                .addOnSuccessListener(aVoid -> {
                    isOnWaitingList = false;
                    updateButtonState();
                    Toast.makeText(this, "Successfully left waiting list", Toast.LENGTH_SHORT).show();
                    checkWaitingListStatus(); // Refresh count
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateButtonState() {
        if (isOnWaitingList) {
            joinLeaveButton.setText("Leave Waiting List");
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
        } else {
            joinLeaveButton.setText("Join Waiting List");
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
        }
    }
}