package com.example.sulfurevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the details of a single event and lets the entrant:
 * <ul>
 *     <li>join or leave the waiting list</li>
 *     <li>accept an invitation (move to enrolled_list)</li>
 *     <li>decline an invitation (move to cancelled_list)</li>
 * </ul>
 *
 * <p>The screen reads the current event document from Firestore and
 * decides what buttons to show based on the device ID.</p>
 */
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

    private boolean isOnWaitingList = false;
    private boolean isInvited = false;
    private boolean isEnrolled = false;
    private boolean isCancelled = false;
    private boolean geolocationEnabled = false; // Track if geolocation is enabled for this event

    /**
     * Called when the detail screen is created.
     * Sets up views, shows the event info from the Intent, and loads the user's status
     * for this event from Firestore.
     *
     * @param savedInstanceState saved UI state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_activity);

        db = FirebaseFirestore.getInstance();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        // Check if user is on waiting list and load event settings
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

    /**
     * Check if location permissions are granted
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permissions from user
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try joining again
                joinWaitingList();
            } else {
                // Permission denied
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
            Log.d(TAG, "Geolocation disabled for this event, joining without location");
            addToWaitingListWithoutLocation();
        }
    }

    /**
     * Add user to waiting list without location (when geolocation is disabled)
     */
    private void addToWaitingListWithoutLocation() {
        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
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
                            // Got location, proceed with joining
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

    /**
     * Request current location if last known location is unavailable
     */
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

                        // Remove location updates after getting one result
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                },
                null
        );
    }

    /**
     * Add user to waiting list and save their location to subcollection
     */
    private void addToWaitingListWithLocation(Double latitude, Double longitude) {
        // First, add user to waiting_list array
        db.collection("Events").document(eventId)
                .update("waiting_list", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(aVoid -> {
                    // Successfully added to waiting list, now save location
                    saveRegistrationLocation(latitude, longitude);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Save the registration location to subcollection
     */
    private void saveRegistrationLocation(Double latitude, Double longitude) {
        // Create location data
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("deviceId", deviceID);
        locationData.put("timestamp", com.google.firebase.Timestamp.now());

        if (latitude != null && longitude != null) {
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("hasLocation", true);

            Log.d(TAG, "Saving location - Lat: " + latitude + ", Lng: " + longitude);
        } else {
            locationData.put("latitude", null);
            locationData.put("longitude", null);
            locationData.put("hasLocation", false);

            Log.d(TAG, "Saving without location data");
        }

        // Save to subcollection: Events/{eventId}/entrant_registration_location/{deviceId}
        db.collection("Events")
                .document(eventId)
                .collection("entrant_registration_location")
                .document(deviceID)
                .set(locationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Location saved successfully to Firestore");
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = true;
                    updateButtonState();
                    Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
                    checkWaitingListStatus(); // Refresh count
                })
                .addOnFailureListener(e -> {
                    // Location save failed, but user is still on waiting list
                    Log.e(TAG, "Failed to save location: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = true;
                    updateButtonState();
                    Toast.makeText(this, "Joined waiting list (location not saved)",
                            Toast.LENGTH_SHORT).show();
                    checkWaitingListStatus(); // Refresh count
                });
    }

    /**
     * Removes the current deviceId from {@code waiting_list} and deletes location data if it exists.
     * On success updates the UI and re-reads the list.
     */
    private void leaveWaitingList() {
        progressBar.setVisibility(View.VISIBLE);
        joinLeaveButton.setEnabled(false);

        // First remove from waiting_list array
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
                        checkWaitingListStatus(); // Refresh count
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Delete the registration location from subcollection
     */
    private void deleteRegistrationLocation() {
        // Delete from subcollection: Events/{eventId}/entrant_registration_location/{deviceId}
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
                    checkWaitingListStatus(); // Refresh count
                })
                .addOnFailureListener(e -> {
                    // Location delete failed, but user was removed from waiting list
                    progressBar.setVisibility(View.GONE);
                    joinLeaveButton.setEnabled(true);
                    isOnWaitingList = false;
                    updateButtonState();
                    Toast.makeText(this, "Left waiting list (location data may remain)",
                            Toast.LENGTH_SHORT).show();
                    checkWaitingListStatus(); // Refresh count
                });
    }

    /**
     * Decides which buttons to show based on the user's current state
     * (waiting, invited, enrolled, cancelled).
     *
     * <p>Priority is:
     * invited → show accept/decline
     * enrolled → disabled "enrolled" button
     * cancelled → disabled "not selected" button
     * else → join/leave
     */
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

    /**
     * Handles the "Accept invitation" path:
     * removes the user from {@code invited_list} and adds them to {@code enrolled_list}.
     * Then checks if the event is full and, if so, notifies the remaining waitlist users.
     */
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

    /**
     * Handles the "Decline invitation" path:
     * removes the user from {@code invited_list} and adds them to {@code cancelled_list}.
     */
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

    /**
     * Small helper to pop up a one-button dialog.
     *
     * @param title   dialog title
     * @param message dialog message
     */
    private void showResultDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }

    /**
     * After an entrant accepts (or after some other path), this checks whether the event
     * is now full based on {@code enrolled_list + invited_list} versus {@code limitGuests}.
     * If it's full, everyone still in {@code waiting_list} (and not already invited/enrolled)
     * gets a "NOT_SELECTED" notification under their profile document.
     *
     * @param eventId id of the event we just updated
     */
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