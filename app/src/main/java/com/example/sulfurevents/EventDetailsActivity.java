package com.example.sulfurevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.*;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FirebaseFirestore db;
    private String deviceID;
    private String eventId;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView eventNameText, descriptionText, organizerText, totalEntrantsText;
    private Button joinLeaveButton, acceptInviteButton, declineInviteButton;
    private ProgressBar progressBar;
    private ImageButton backButton;
    private ImageView eventPoster;

    private boolean isOnWaitingList = false;
    private boolean isInvited = false;
    private boolean isEnrolled = false;
    private boolean isCancelled = false;
    private boolean geolocationEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_activity);

        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupBackButton();

        Intent intent = getIntent();
        handleIntent(intent);

        joinLeaveButton.setOnClickListener(v -> {
            if (isOnWaitingList) leaveWaitingList();
            else joinWaitingList();
        });
    }

    private void initViews() {
        eventNameText = findViewById(R.id.event_name_detail);
        descriptionText = findViewById(R.id.event_description);
        organizerText = findViewById(R.id.event_organizer);
        totalEntrantsText = findViewById(R.id.total_entrants);
        joinLeaveButton = findViewById(R.id.join_leave_button);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.back_button_details);
        acceptInviteButton = findViewById(R.id.accept_invite_button);
        declineInviteButton = findViewById(R.id.decline_invite_button);
        eventPoster = findViewById(R.id.EntrantEventImage);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Shows a styled alert dialog matching the app's black and gold theme
     * Auto-dismisses after 2.5 seconds
     */
    private void showStyledDialog(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            // Set background color
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(0xFF000000)
                );
            }

            // Style message text - White
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(0xFFFFFFFF); // White
            }

            // Style title - Gold
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView titleView = dialog.findViewById(titleId);
            if (titleView != null) {
                titleView.setTextColor(0xFFD4AF37); // Gold
            }

            // Auto-dismiss after 2.5 seconds
            new android.os.Handler().postDelayed(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }, 2500);
        });

        dialog.show();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                eventId = uri.getLastPathSegment();
                loadEventDetailsFromFirestore();
                return;
            }
        }

        eventId = intent.getStringExtra("eventId");
        if (eventId == null) eventId = intent.getStringExtra("EVENT_ID");
        String eventName = intent.getStringExtra("eventName");
        String description = intent.getStringExtra("description");
        String organizer = intent.getStringExtra("organizerEmail");
        String posterURL = intent.getStringExtra("posterURL");

        displayEventInfo(eventName, description, organizer, posterURL);

        if (applyDateRestrictions()) return;
        checkWaitingListStatus();
    }

    private void displayEventInfo(String name, String description, String organizer, String posterURL) {
        eventNameText.setText(name);
        descriptionText.setText(description != null ? description : "No description available");
        organizerText.setText("Organizer: " + (organizer != null ? organizer : "Unknown"));

        if (posterURL != null && !posterURL.isEmpty())
            Glide.with(this).load(posterURL).into(eventPoster);
        else
            eventPoster.setImageResource(R.drawable.outline_ad_off_24);
    }

    private void checkWaitingListStatus() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        updateFlags(doc);
                        totalEntrantsText.setText("Total Entrants: " + getWaitingList(doc).size());
                    }
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    updateButtonState();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    showStyledDialog("Error", "Error loading event details");
                });
    }

    private void updateFlags(com.google.firebase.firestore.DocumentSnapshot doc) {
        isOnWaitingList = getWaitingList(doc).contains(deviceID);
        isInvited = getInvitedList(doc).contains(deviceID);
        isEnrolled = getEnrolledList(doc).contains(deviceID);
        isCancelled = getCancelledList(doc).contains(deviceID);
        Boolean geoEnabled = doc.getBoolean("geolocationEnabled");
        geolocationEnabled = geoEnabled != null && geoEnabled;
    }

    private List<String> getWaitingList(com.google.firebase.firestore.DocumentSnapshot doc) {
        List<String> list = (List<String>) (doc.get("waiting_list") != null ? doc.get("waiting_list") : doc.get("waitingList"));
        return list != null ? list : new ArrayList<>();
    }
    private List<String> getInvitedList(com.google.firebase.firestore.DocumentSnapshot doc) {
        List<String> list = (List<String>) (doc.get("invited_list") != null ? doc.get("invited_list") : doc.get("invitedList"));
        return list != null ? list : new ArrayList<>();
    }
    private List<String> getEnrolledList(com.google.firebase.firestore.DocumentSnapshot doc) {
        List<String> list = (List<String>) (doc.get("enrolled_list") != null ? doc.get("enrolled_list") : doc.get("enrolledList"));
        return list != null ? list : new ArrayList<>();
    }
    private List<String> getCancelledList(com.google.firebase.firestore.DocumentSnapshot doc) {
        List<String> list = (List<String>) (doc.get("cancelled_list") != null ? doc.get("cancelled_list") : doc.get("cancelledList"));
        return list != null ? list : new ArrayList<>();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                joinWaitingList();
            else {
                progressBar.setVisibility(View.GONE);
                joinLeaveButton.setEnabled(true);
                showStyledDialog("Permission Required", "Location permission is required to join the waitlist");
            }
        }
    }

    private void joinWaitingList() {
        if (!checkLocationPermission()) { requestLocationPermission(); return; }
        if (applyDateRestrictions()) return;

        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        if (geolocationEnabled) getCurrentLocationAndJoin();
        else addToWaitingListWithoutLocation();
    }

    private void addToWaitingListWithoutLocation() {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        showStyledDialog("Error", "Event not found");
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        return;
                    }
                    List<String> waiting = getWaitingList(doc);
                    int limit = parseLimit(doc.getString("waitingListLimit"));
                    if (limit > 0 && waiting.size() >= limit) {
                        showStyledDialog("Waitlist Full", "The waiting list is full");
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Waiting List Full")
                                .setMessage("Waiting list is full for this event.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    db.collection("Events").document(eventId)
                            .update("waiting_list", FieldValue.arrayUnion(deviceID))
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                isOnWaitingList = true;
                                updateButtonState();

                                showStyledDialog("Success", "Successfully Joined Waiting List!");
                                createJoinNotification(eventNameText.getText().toString());

                                // Create notification for event history (US 01.02.03)
                                String eventName = eventNameText.getText().toString();
                                createJoinNotification(eventName);


                                checkWaitingListStatus();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                showStyledDialog("Error", "Failed to join waiting list");
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    showStyledDialog("Error", "Failed to load event");
                });
    }

    private int parseLimit(String str) {
        try { return str != null ? Integer.parseInt(str.trim()) : -1; }
        catch (Exception e) { return -1; }
    }

    private void getCurrentLocationAndJoin() {
        if (!checkLocationPermission()) { progressBar.setVisibility(View.GONE); joinLeaveButton.setEnabled(true); return; }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null)
                        addToWaitingListWithLocation(location.getLatitude(), location.getLongitude());
                    else requestCurrentLocation();
                })
                .addOnFailureListener(e -> requestCurrentLocation());
    }

    private void requestCurrentLocation() {
        if (!checkLocationPermission()) { addToWaitingListWithLocation(null, null); return; }

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1).build();

        fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) addToWaitingListWithLocation(loc.getLatitude(), loc.getLongitude());
                else addToWaitingListWithLocation(null, null);
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, null);
    }

private void addToWaitingListWithLocation(Double lat, Double lng) {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        showStyledDialog("Error", "Event not found");
                        return;
                    }
                    
                    List<String> waiting = (List<String>) (
                            doc.get("waiting_list") != null ?
                                    doc.get("waiting_list") :
                                    doc.get("waitingList")
                    );
                    if (waiting == null) waiting = new ArrayList<>();

                    String waitingLimitStr = doc.getString("waitingListLimit");
                    int waitingLimit = -1;

                    if (waitingLimitStr != null && !waitingLimitStr.isEmpty()) {
                        try {
                            waitingLimit = Integer.parseInt(waitingLimitStr.trim());
                        } catch (NumberFormatException ignored) {
                            waitingLimit = -1;
                        }
                    }

                    // Enforce waiting list limit
                    if (waitingLimit > 0 && waiting.size() >= waitingLimit) {
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        showStyledDialog("Waitlist Full", "The waiting list is full for this event");
                        return;
                    }

                    // Add user to waiting_list array
                    db.collection("Events").document(eventId)
                            .update("waiting_list", FieldValue.arrayUnion(deviceID))
                            .addOnSuccessListener(aVoid -> {
                                saveRegistrationLocation(lat, lng);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                showStyledDialog("Error", "Failed to join waiting list");
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    showStyledDialog("Error", "Failed to load event");
                });
    }

    private void leaveWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayRemove(deviceID))
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed from waiting list
                    if (geolocationEnabled) {
                        // Delete location data if geolocation was enabled
                        deleteRegistrationLocation();
                    } else {
                        // No location data to delete
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        isOnWaitingList = false;
                        updateButtonState();
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setMessage("Successfully left waiting list!")
                                .setPositiveButton("OK", null)
                                .show();

                        // Create notification for event history (US 01.02.03)
                        String eventName = eventNameText.getText().toString();
                        createLeaveNotification(eventName);

                        checkWaitingListStatus(); // Refresh count
                    }

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    showStyledDialog("Error", "Failed to leave waiting list");
                });
    }

private void deleteRegistrationLocation() {
        db.collection("Events").document(eventId)
                .collection("entrant_registration_location").document(deviceID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = false;
                    updateButtonState();
                    showStyledDialog("Success", "Successfully left the waiting list!");
                    
                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createLeaveNotification(eventName);
                    
                    checkWaitingListStatus();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = false;
                    updateButtonState();
                    showStyledDialog("Partial Success", "Left waiting list (location data may remain)");
                    
                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createLeaveNotification(eventName);
                    
                    checkWaitingListStatus();
                });
    }

    private void finalizeLeave() {
        progressBar.setVisibility(View.GONE);
        joinLeaveButton.setEnabled(true);
        isOnWaitingList = false;
        updateButtonState();
        showStyledDialog("Success", "Successfully Left Waiting List!");
        createLeaveNotification(eventNameText.getText().toString());
        checkWaitingListStatus();
    }

    private void finalizeLeave() {
        progressBar.setVisibility(View.GONE);
        joinLeaveButton.setEnabled(true);
        isOnWaitingList = false;
        updateButtonState();
        showStyledDialog("Success", "Successfully Left Waiting List!");
        createLeaveNotification(eventNameText.getText().toString());
        checkWaitingListStatus();
    }

    private void updateButtonState() {
        acceptInviteButton.setVisibility(View.GONE);
        declineInviteButton.setVisibility(View.GONE);
        joinLeaveButton.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(true);

        if (isEnrolled) {
            joinLeaveButton.setText("You are enrolled");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
            return;
        }
        if (isCancelled) {
            joinLeaveButton.setText("You were not selected");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
            return;
        }
        if (isInvited) {
            joinLeaveButton.setVisibility(View.GONE);
            acceptInviteButton.setVisibility(View.VISIBLE);
            declineInviteButton.setVisibility(View.VISIBLE);
            acceptInviteButton.setOnClickListener(v -> acceptInvitation());
            declineInviteButton.setOnClickListener(v -> declineInvitation());
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

    private boolean applyDateRestrictions() {
        String start = getIntent().getStringExtra("startDate");
        String end = getIntent().getStringExtra("endDate");

        if (start == null || end == null || start.isEmpty() || end.isEmpty()) return false;

        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date now = new Date();
            Date sDate = df.parse(start);
            Date eDate = df.parse(end);
            Calendar cal = Calendar.getInstance();
            cal.setTime(eDate); cal.add(Calendar.DAY_OF_MONTH, 1); cal.add(Calendar.MILLISECOND, -1); eDate = cal.getTime();

            if (now.before(sDate)) { joinLeaveButton.setText("Waitlist opens on " + start); joinLeaveButton.setEnabled(false); joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light)); return true; }
            if (now.after(eDate)) { joinLeaveButton.setText("Cannot join (Registration closed)"); joinLeaveButton.setEnabled(false); joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light)); return true; }

        } catch (Exception ex) { joinLeaveButton.setText("Invalid event dates"); joinLeaveButton.setEnabled(false); joinLeaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light)); return true; }
        return false;
    }

    private void loadEventDetailsFromFirestore() {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        showStyledDialog("Error", "Event not found");
                        return;
                    }
                    displayEventInfo(doc.getString("eventName"), doc.getString("description"), doc.getString("organizerEmail"), doc.getString("posterURL"));
                    checkWaitingListStatus();
                })
                .addOnFailureListener(e -> showStyledDialog("Error", "Error loading event"));
    }

    private void createJoinNotification(String eventName) { createNotification("WAITING", "You joined the waiting list for " + eventName); }
    private void createLeaveNotification(String eventName) { createNotification("LEFT_WAITLIST", "You left the waiting list for " + eventName); }
    private void createAcceptNotification(String eventName) { createNotification("ENROLLED", "You accepted the invitation and are now enrolled in " + eventName); }
    private void createDeclineNotification(String eventName) { createNotification("CANCELLED", "You declined the invitation for " + eventName); }

    private void createNotification(String type, String message) {
        Map<String, Object> notif = new HashMap<>();
        notif.put("eventId", eventId);
        notif.put("eventName", eventNameText.getText().toString());
        notif.put("type", type);
        notif.put("message", message);
        notif.put("timestamp", System.currentTimeMillis());
        notif.put("read", false);
        db.collection("Profiles").document(deviceID).collection("notifications").add(notif);
    }

    private void acceptInvitation() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Events").document(eventId)
                .update("invited_list", FieldValue.arrayRemove(deviceID), "enrolled_list", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false; isEnrolled = true;
                    updateButtonState();
                    createAcceptNotification(eventNameText.getText().toString());
                    showStyledDialog("Success", "Invitation accepted");
                    checkAndNotifyNotSelectedIfFull(eventId);
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showStyledDialog("Error", "Couldn't accept invitation");
                });
    }

    private void declineInvitation() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Events").document(eventId)
                .update("invited_list", FieldValue.arrayRemove(deviceID), "cancelled_list", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false; isCancelled = true;
                    updateButtonState();
                    createDeclineNotification(eventNameText.getText().toString());
                    showStyledDialog("Invitation Declined", "Invitation declined");
                    drawReplacementAfterDecline();
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showStyledDialog("Error", "Couldn't decline invitation");
                });
    }

    private void drawReplacementAfterDecline() {
        db.collection("Events").document(eventId).get().addOnSuccessListener(doc -> {
            List<String> waitingList = getWaitingList(doc);
            int limit = parseLimit(doc.getString("waitingListLimit"));
            List<String> enrolled = getEnrolledList(doc);
            int spotsAvailable = limit - enrolled.size();
            if (spotsAvailable <= 0 || waitingList.isEmpty()) return;
            String nextInLine = waitingList.get(0);
            db.collection("Events").document(eventId)
                    .update("waiting_list", FieldValue.arrayRemove(nextInLine), "enrolled_list", FieldValue.arrayUnion(nextInLine));
        });
    }

    private void checkAndNotifyNotSelectedIfFull(String eventId) {
        db.collection("Events").document(eventId).get().addOnSuccessListener(doc -> {
            List<String> waitingList = getWaitingList(doc);
            int limit = parseLimit(doc.getString("waitingListLimit"));
            List<String> enrolled = getEnrolledList(doc);
            if (enrolled.size() >= limit) {
                for (String id : waitingList) {
                    Map<String,Object> notif = new HashMap<>();
                    notif.put("eventId", eventId);
                    notif.put("type","NOT_SELECTED");
                    notif.put("message","You were not selected for " + eventNameText.getText().toString());
                    notif.put("timestamp", System.currentTimeMillis());
                    notif.put("read", false);
                    db.collection("Profiles").document(id).collection("notifications").add(notif);
                }
            }
        });
    }
}