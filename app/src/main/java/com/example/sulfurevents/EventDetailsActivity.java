package com.example.sulfurevents;


import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventDetailsActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private String deviceID;
    private String eventId;


    private TextView eventNameText, descriptionText, organizerText, totalEntrantsText;
    private Button joinLeaveButton;
    private Button acceptInviteButton;
    private Button declineInviteButton;
    private ProgressBar progressBar;
    private ImageButton backButton;


    private boolean isOnWaitingList = false;
    private boolean isInvited = false;
    private boolean isEnrolled = false;
    private boolean isCancelled = false;


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
        acceptInviteButton = findViewById(R.id.accept_invite_button);
        declineInviteButton = findViewById(R.id.decline_invite_button);


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


        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayRemove(deviceID))
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
        // default: show join/leave
        joinLeaveButton.setVisibility(View.VISIBLE);
        acceptInviteButton.setVisibility(View.GONE);
        declineInviteButton.setVisibility(View.GONE);

        if (isInvited) {
            // entrant has been chosen → show accept/decline
            joinLeaveButton.setVisibility(View.GONE);
            acceptInviteButton.setVisibility(View.VISIBLE);
            declineInviteButton.setVisibility(View.VISIBLE);

            acceptInviteButton.setOnClickListener(v -> acceptInvitation());
            declineInviteButton.setOnClickListener(v -> declineInvitation());
            return;
        }

        if (isEnrolled) {
            // lock it
            joinLeaveButton.setText("You are enrolled");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_green_light)
            );
            return;
        }

        if (isCancelled) {
            joinLeaveButton.setText("You were not selected");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_red_light)
            );
            return;
        }
        if (isOnWaitingList) {
            joinLeaveButton.setText("Leave Waiting List");
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
        } else {
            joinLeaveButton.setText("Join Waiting List");
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
        }
    }
    private void acceptInvitation() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Events").document(eventId)
                .update(
                        "invited_list", com.google.firebase.firestore.FieldValue.arrayRemove(deviceID),
                        "enrolled_list", com.google.firebase.firestore.FieldValue.arrayUnion(deviceID)
                )
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false;
                    isEnrolled = true;
                    updateButtonState();
                    showResultDialog("Invitation accepted", "You are now enrolled for this event.");

                    // after someone accepts, check if event is now full / deadline passed
                    checkAndNotifyNotSelectedIfFull(eventId);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showResultDialog("Error", "Couldn't accept: " + e.getMessage());
                });
    }

    private void declineInvitation() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Events").document(eventId)
                .update(
                        "invited_list", com.google.firebase.firestore.FieldValue.arrayRemove(deviceID),
                        "cancelled_list", com.google.firebase.firestore.FieldValue.arrayUnion(deviceID)
                )
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false;
                    isCancelled = true;
                    updateButtonState();
                    showResultDialog("Invitation declined", "You declined the invitation for this event.");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showResultDialog("Error", "Couldn't decline: " + e.getMessage());
                });
    }

    private void showResultDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
    private void checkAndNotifyNotSelectedIfFull(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) return;

                    // 1) name
                    String eventName = eventDoc.getString("eventName");
                    if (eventName == null) eventName = "Event";

                    // 2) lists (tolerant like the rest of your project)
                    @SuppressWarnings("unchecked")
                    List<String> waiting  = (List<String>) (eventDoc.get("waiting_list") != null
                            ? eventDoc.get("waiting_list")
                            : eventDoc.get("waitingList"));
                    if (waiting == null) waiting = new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<String> enrolled = (List<String>) (eventDoc.get("enrolled_list") != null
                            ? eventDoc.get("enrolled_list")
                            : eventDoc.get("enrolledList"));
                    if (enrolled == null) enrolled = new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<String> invited  = (List<String>) (eventDoc.get("invited_list") != null
                            ? eventDoc.get("invited_list")
                            : eventDoc.get("invitedList"));
                    if (invited == null) invited = new ArrayList<>();

                    // 3) capacity — your app uses "limitGuests" as STRING
                    String capStr = eventDoc.getString("limitGuests");
                    int capacity = 0;
                    try {
                        capacity = Integer.parseInt(capStr);
                    } catch (Exception ignored) {
                        // if it's missing or bad, we just don't send not-selected
                    }

                    if (capacity <= 0) {
                        // no capacity recorded → nothing to do
                        return;
                    }

                    // 4) in YOUR codebase an invited person also occupies a slot
                    int currentlyTaken = enrolled.size() + invited.size();
                    boolean isFull = currentlyTaken >= capacity;

                    if (!isFull) {
                        return; // still room, don't tell people "not selected" yet
                    }

                    // 5) event is full → tell everyone still waiting (and not already invited/enrolled)
                    for (String deviceId : waiting) {
                        if (enrolled.contains(deviceId)) continue;
                        if (invited.contains(deviceId)) continue;

                        Map<String, Object> notif = new HashMap<>();
                        notif.put("eventId", eventId);
                        notif.put("eventName", eventName);
                        notif.put("type", "NOT_SELECTED");
                        notif.put("message", "You were not selected for " + eventName + ".");
                        notif.put("timestamp", System.currentTimeMillis());
                        notif.put("read", false);

                        db.collection("Profiles")
                                .document(deviceId)
                                .collection("notifications")
                                .add(notif);
                    }

                    // optional: clear waiting list since no more room
                    db.collection("Events").document(eventId)
                            .update("waiting_list", new ArrayList<String>());
                });
    }


}