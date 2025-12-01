package com.example.sulfurevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FirebaseFirestore db;
    private String deviceID;
    private String eventId;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView eventNameText, descriptionText, organizerText, totalEntrantsText;
    private Button joinLeaveButton;
    private Button acceptInviteButton;
    private Button declineInviteButton;
    private ProgressBar progressBar;
    private ImageButton backButton;
    private ImageView EventPoster;

    private boolean isOnWaitingList = false;
    private boolean isInvited = false;
    private boolean isEnrolled = false;
    private boolean isCancelled = false;
    private boolean geolocationEnabled = false; // Track if geolocation is enabled for this event

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_activity);

        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize all UI views FIRST
        eventNameText = findViewById(R.id.event_name_detail);
        descriptionText = findViewById(R.id.event_description);
        organizerText = findViewById(R.id.event_organizer);
        totalEntrantsText = findViewById(R.id.total_entrants);
        joinLeaveButton = findViewById(R.id.join_leave_button);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.back_button_details);
        acceptInviteButton = findViewById(R.id.accept_invite_button);
        declineInviteButton = findViewById(R.id.decline_invite_button);
        EventPoster = findViewById(R.id.EntrantEventImage);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Handle deep link (QR scan)
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                eventId = uri.getLastPathSegment();
                loadEventDetailsFromFirestore();
                return;
            }
        }

        // Normal navigation (coming from list)
        eventId = intent.getStringExtra("eventId");
        String eventName = intent.getStringExtra("eventName");
        String description = intent.getStringExtra("description");
        String organizer = intent.getStringExtra("organizerEmail");
        String posterURL = intent.getStringExtra("posterURL");

        eventNameText.setText(eventName);
        descriptionText.setText(description != null ? description : "No description available");
        organizerText.setText("Organizer: " + (organizer != null ? organizer : "Unknown"));

        // Set the image poster
        if (posterURL != null && !posterURL.isEmpty()) {
            Glide.with(this).load(posterURL).into(EventPoster);
        } else {
            EventPoster.setImageResource(R.drawable.outline_ad_off_24);
        }


        // Check if user is on waiting list and load entrant count
        if (applyDateRestrictions()) {
            return; // Stop here if registration is closed
        }

        checkWaitingListStatus();

        joinLeaveButton.setOnClickListener(v -> {
            if (isOnWaitingList) {
                leaveWaitingList();
            } else {
                joinWaitingList();
            }
        });
    }

    /**
     * Reads the event document to see:
     * <ul>
     *     <li>if this device is in {@code waiting_list}</li>
     *     <li>how many entrants are in the list</li>
     *     <li>if geolocation is enabled for this event</li>
     * </ul>
     * Then updates the UI appropriately.
     */
    private void checkWaitingListStatus() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> waitingList = (List<String>) documentSnapshot.get("waiting_list");

                        // Check if geolocation is enabled for this event
                        Boolean geoEnabled = documentSnapshot.getBoolean("geolocationEnabled");
                        geolocationEnabled = (geoEnabled != null && geoEnabled);

                        Log.d(TAG, "Event geolocation enabled: " + geolocationEnabled);

                        if (waitingList != null) {
                            isOnWaitingList = waitingList.contains(deviceID);
                            updateButtonState();
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

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinWaitingList();
            } else {
                progressBar.setVisibility(View.GONE);
                joinLeaveButton.setEnabled(true);
                Toast.makeText(this, "Location permission is required to join the waiting list",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Adds the current deviceId to {@code waiting_list} in Firestore and saves location if enabled.
     * On success updates the UI and re-reads the list to refresh the count.
     */
    private void joinWaitingList() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (applyDateRestrictions()) return;

        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        // Check if geolocation is enabled for this event
        if (geolocationEnabled) {
            // Geolocation is enabled - check permission and get location
            if (!checkLocationPermission()) {
                requestLocationPermission();
                return;
            }
            // Get current location first, then add to waiting list
            getCurrentLocationAndJoin();
        } else {
            // Geolocation is disabled - just add to waiting list without location
            Log.d("EventDetails", "Geolocation disabled for this event, joining without location");
            addToWaitingListWithoutLocation();
        }
    }

    /**
     * Add user to waiting list without location (when geolocation is disabled)
     */
    private void addToWaitingListWithoutLocation() {
        // Check waiting list limit BEFORE adding
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Waiting list is full for this event.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add to waiting list without location
                    db.collection("Events").document(eventId)
                            .update("waiting_list", FieldValue.arrayUnion(deviceID))
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                isOnWaitingList = true;
                                updateButtonState();
                                Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();

                                // Create notification for event history (US 01.02.03)
                                String eventName = eventNameText.getText().toString();
                                createJoinNotification(eventName);

                                checkWaitingListStatus();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                Toast.makeText(this, "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Get current location and add user to waiting list (when geolocation is enabled)
     */
    private void getCurrentLocationAndJoin() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            progressBar.setVisibility(View.GONE);
            joinLeaveButton.setEnabled(true);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Log.d(TAG, "Location obtained - Lat: " + latitude + ", Lng: " + longitude);

                            addToWaitingListWithLocation(latitude, longitude);
                        } else {
                            // Location is null - try to get current location instead of last known
                            Log.d(TAG, "Last location is null, requesting current location");
                            requestCurrentLocation();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    // Failed to get location, try requesting current location
                    requestCurrentLocation();
                });
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            addToWaitingListWithLocation(null, null);
            return;
        }

        com.google.android.gms.location.LocationRequest locationRequest =
                new com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Log.d(TAG, "Current location obtained - Lat: " + latitude + ", Lng: " + longitude);

                            addToWaitingListWithLocation(latitude, longitude);
                        } else {
                            Log.d(TAG, "Current location is also null");
                            addToWaitingListWithLocation(null, null);
                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                },
                null
        );
    }

    private void addToWaitingListWithLocation(Double latitude, Double longitude) {
        // Check waiting list limit BEFORE adding
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        progressBar.setVisibility(View.GONE);
                        joinLeaveButton.setEnabled(true);
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Waiting list is full for this event.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add user to waiting_list array
                    db.collection("Events").document(eventId)
                            .update("waiting_list", FieldValue.arrayUnion(deviceID))
                            .addOnSuccessListener(aVoid -> {
                                saveRegistrationLocation(latitude, longitude);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                joinLeaveButton.setEnabled(true);
                                Toast.makeText(this, "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRegistrationLocation(Double latitude, Double longitude) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("deviceId", deviceID);
        locationData.put("timestamp", com.google.firebase.Timestamp.now());

        if (latitude != null && longitude != null) {
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("hasLocation", true);
            Log.d("EventDetails", "Saving location - Lat: " + latitude + ", Lng: " + longitude);
        } else {
            locationData.put("latitude", null);
            locationData.put("longitude", null);
            locationData.put("hasLocation", false);
            Log.d("EventDetails", "Saving without location data");
        }

        db.collection("Events")
                .document(eventId)
                .collection("entrant_registration_location")
                .document(deviceID)
                .set(locationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventDetails", "Location saved successfully to Firestore");
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = true;
                    updateButtonState();
                    Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();

                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createJoinNotification(eventName);

                    checkWaitingListStatus();
                })
                .addOnFailureListener(e -> {
                    // Location save failed, but user is still on waiting list
                    Log.e(TAG, "Failed to save location: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = true;
                    updateButtonState();
                    Toast.makeText(this, "Joined waiting list (location not saved)", Toast.LENGTH_SHORT).show();

                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createJoinNotification(eventName);

                    checkWaitingListStatus();
                });
    }

    /**
     * Removes the current deviceId from {@code waiting_list} and deletes location data if it exists.
     * On success updates the UI and re-reads the list.
     */
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
                        Toast.makeText(this, "Successfully left waiting list", Toast.LENGTH_SHORT).show();

                        // Create notification for event history (US 01.02.03)
                        String eventName = eventNameText.getText().toString();
                        createLeaveNotification(eventName);

                        checkWaitingListStatus(); // Refresh count
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteRegistrationLocation() {
        db.collection("Events")
                .document(eventId)
                .collection("entrant_registration_location")
                .document(deviceID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = false;
                    updateButtonState();
                    Toast.makeText(this, "Successfully left waiting list", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(this, "Left waiting list (location data may remain)", Toast.LENGTH_SHORT).show();

                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createLeaveNotification(eventName);

                    checkWaitingListStatus();
                });
    }

    private void updateButtonState() {
        if (applyDateRestrictions()) return;

        joinLeaveButton.setVisibility(View.VISIBLE);
        acceptInviteButton.setVisibility(View.GONE);
        declineInviteButton.setVisibility(View.GONE);

        if (isInvited) {
            joinLeaveButton.setVisibility(View.GONE);
            acceptInviteButton.setVisibility(View.VISIBLE);
            declineInviteButton.setVisibility(View.VISIBLE);
            acceptInviteButton.setOnClickListener(v -> acceptInvitation());
            declineInviteButton.setOnClickListener(v -> declineInvitation());
            return;
        }

        if (isEnrolled) {
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
                        "invited_list", FieldValue.arrayRemove(deviceID),
                        "enrolled_list", FieldValue.arrayUnion(deviceID)
                )
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false;
                    isEnrolled = true;
                    updateButtonState();

                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createAcceptNotification(eventName);

                    showResultDialog("Invitation accepted", "You are now enrolled for this event.");
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
                        "invited_list", FieldValue.arrayRemove(deviceID),
                        "cancelled_list", FieldValue.arrayUnion(deviceID)
                )
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    isInvited = false;
                    isCancelled = true;
                    updateButtonState();

                    // Create notification for event history (US 01.02.03)
                    String eventName = eventNameText.getText().toString();
                    createDeclineNotification(eventName);

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
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) return;

                    String eventName = eventDoc.getString("eventName");
                    if (eventName == null) eventName = "Event";

                    List<String> waiting = (List<String>) (eventDoc.get("waiting_list") != null
                            ? eventDoc.get("waiting_list")
                            : eventDoc.get("waitingList"));
                    if (waiting == null) waiting = new ArrayList<>();

                    List<String> enrolled = (List<String>) (eventDoc.get("enrolled_list") != null
                            ? eventDoc.get("enrolled_list")
                            : eventDoc.get("enrolledList"));
                    if (enrolled == null) enrolled = new ArrayList<>();

                    List<String> invited = (List<String>) (eventDoc.get("invited_list") != null
                            ? eventDoc.get("invited_list")
                            : eventDoc.get("invitedList"));
                    if (invited == null) invited = new ArrayList<>();

                    String capStr = eventDoc.getString("limitGuests");
                    int capacity = 0;
                    try {
                        capacity = Integer.parseInt(capStr);
                    } catch (Exception ignored) {
                    }

                    if (capacity <= 0) return;

                    int currentlyTaken = enrolled.size() + invited.size();
                    boolean isFull = currentlyTaken >= capacity;

                    if (!isFull) return;

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

                        sendNotifIfEnabled(deviceId, notif);
                    }

                    db.collection("Events").document(eventId)
                            .update("waiting_list", new ArrayList<String>());
                });
    }

    private void loadEventDetailsFromFirestore() {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String eventName = doc.getString("eventName");
                    String description = doc.getString("description");
                    String organizerEmail = doc.getString("organizerEmail");
                    String posterURL = doc.getString("posterURL");

                    eventNameText.setText(eventName);
                    descriptionText.setText(description);
                    organizerText.setText("Organizer: " + organizerEmail);

                    if (posterURL != null && !posterURL.isEmpty()) {
                        Glide.with(this).load(posterURL).into(EventPoster);
                    } else {
                        EventPoster.setImageResource(R.drawable.outline_ad_off_24);
                    }

                    checkWaitingListStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotifIfEnabled(String targetId, Map<String, Object> notif) {
        db.collection("Profiles").document(targetId).get()
                .addOnSuccessListener(doc -> {
                    Boolean enabled = doc.getBoolean("notificationsEnabled");
                    if (enabled == null || enabled) {
                        db.collection("Profiles")
                                .document(targetId)
                                .collection("notifications")
                                .add(notif);
                    }
                });
    }

    private boolean applyDateRestrictions() {
        String start = getIntent().getStringExtra("startDate");
        String end = getIntent().getStringExtra("endDate");

        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date now = new Date();
            Date sDate = df.parse(start);
            Date eDate = df.parse(end);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(eDate);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1); // Move to next day
            cal.add(java.util.Calendar.MILLISECOND, -1); // Subtract 1ms to get 23:59:59.999
            eDate = cal.getTime();



            if (now.before(sDate)) {
                joinLeaveButton.setText("Waitlist opens on " + start);
                joinLeaveButton.setEnabled(false);
                joinLeaveButton.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.holo_red_light)
                );
                return true;
            }

            if (now.after(eDate)) {
                joinLeaveButton.setText("Cannot join (Registration closed)");
                joinLeaveButton.setEnabled(false);
                joinLeaveButton.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.holo_red_light)
                );
                return true;
            }

        } catch (Exception ex) {
            joinLeaveButton.setText("Invalid event dates");
            joinLeaveButton.setEnabled(false);
            joinLeaveButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_red_light)
            );
            return true;
        }

        return false;
    }

    /**
     * Creates a notification in the user's history when they join the waiting list
     * This supports US 01.02.03 - Event History feature
     */
    private void createJoinNotification(String eventName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "WAITING");
        notification.put("message", "You joined the waiting list for " + eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Join notification created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create join notification: " + e.getMessage());
                });
    }

    /**
     * Creates a notification when user leaves the waiting list
     */
    private void createLeaveNotification(String eventName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "LEFT_WAITLIST");
        notification.put("message", "You left the waiting list for " + eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Leave notification created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create leave notification: " + e.getMessage());
                });
    }

    /**
     * Creates a notification when user accepts an invitation
     */
    private void createAcceptNotification(String eventName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "ENROLLED");
        notification.put("message", "You accepted the invitation and are now enrolled in " + eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Accept notification created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create accept notification: " + e.getMessage());
                });
    }

    /**
     * Creates a notification when user declines an invitation
     */
    private void createDeclineNotification(String eventName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("type", "CANCELLED");
        notification.put("message", "You declined the invitation for " + eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Profiles")
                .document(deviceID)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Decline notification created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create decline notification: " + e.getMessage());
                });
    }

}
