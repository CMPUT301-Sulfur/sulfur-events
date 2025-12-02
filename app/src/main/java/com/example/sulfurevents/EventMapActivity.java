package com.example.sulfurevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventMapActivity displays all waitlist sign-up locations for a specific event on a Google Map.
 * It fetches location data from Firestore subcollection: Events/{eventId}/entrant_registration_location
 * If geolocation is disabled for the event, it shows a message instead of the map.
 */
public class EventMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "EventMapActivity";

    private GoogleMap mMap;
    private FirebaseFirestore db;

    private TextView eventNameText;
    private TextView waitlistCountText;
    private ProgressBar loadingIndicator;
    private SupportMapFragment mapFragment;

    private String eventId;
    private String eventName;
    private List<EntrantLocation> entrantLocations;
    private Map<String, String> markerToDeviceIdMap; // Maps marker ID to device ID
    private boolean geolocationEnabled = true; // Default to true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_list_map);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        entrantLocations = new ArrayList<>();
        markerToDeviceIdMap = new HashMap<>();

        // Get event info from Intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        eventNameText = findViewById(R.id.eventNameText);
        waitlistCountText = findViewById(R.id.waitlistCountText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Set event name
        if (eventName != null) {
            eventNameText.setText(eventName);
        } else {
            eventNameText.setText("Event Map");
        }

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Back button
        ImageButton backButton = findViewById(R.id.backButtonMap);
        backButton.setOnClickListener(v -> finish());

        // Check if geolocation is enabled before loading map
        checkGeolocationStatus();
    }

    /**
     * Check if geolocation is enabled for this event
     */
    private void checkGeolocationStatus() {
        loadingIndicator.setVisibility(View.VISIBLE);

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean geoEnabled = documentSnapshot.getBoolean("geolocationEnabled");
                        geolocationEnabled = (geoEnabled != null && geoEnabled);

                        Log.d(TAG, "Geolocation enabled: " + geolocationEnabled);

                        if (geolocationEnabled) {
                            // Geolocation is enabled - load the map
                            if (mapFragment != null) {
                                mapFragment.getMapAsync(this);
                            }
                        } else {
                            // Geolocation is disabled - show message and hide map
                            loadingIndicator.setVisibility(View.GONE);
                            waitlistCountText.setText("Geolocation Not Enabled");

                            // Hide the map fragment
                            if (mapFragment != null) {
                                getSupportFragmentManager().beginTransaction()
                                        .hide(mapFragment)
                                        .commit();
                            }

                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Geolocation Disabled")
                                    .setMessage("Geolocation tracking is not enabled for this event.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } else {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Error checking geolocation status: " + e.getMessage());
                    Toast.makeText(this,
                            "Error loading event details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        // Set initial camera position (world view)
        LatLng initialPosition = new LatLng(20, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 2));

        // Set up marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            String deviceId = markerToDeviceIdMap.get(marker.getId());
            if (deviceId != null) {
                fetchAndDisplayUserDetails(deviceId);
            }
            return false; // Return false to show default info window as well
        });

        // Set up info window click listener for more details
        mMap.setOnInfoWindowClickListener(marker -> {
            String deviceId = markerToDeviceIdMap.get(marker.getId());
            if (deviceId != null) {
                fetchAndDisplayUserDetails(deviceId);
            }
        });

        // Load event sign-up locations from Firestore
        loadEventSignUpLocations();
    }

    /**
     * Fetches all entrant registration locations for this event from Firestore
     * Path: Events/{eventId}/entrant_registration_location
     */
    private void loadEventSignUpLocations() {
        loadingIndicator.setVisibility(View.VISIBLE);

        db.collection("Events")
                .document(eventId)
                .collection("entrant_registration_location")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantLocations.clear();
                    int validLocationCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String deviceId = document.getString("deviceId");
                            Boolean hasLocation = document.getBoolean("hasLocation");

                            // Check if location data exists
                            if (hasLocation != null && hasLocation) {
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");

                                if (latitude != null && longitude != null) {
                                    EntrantLocation location = new EntrantLocation(
                                            deviceId,
                                            latitude,
                                            longitude
                                    );
                                    entrantLocations.add(location);
                                    validLocationCount++;

                                    Log.d(TAG, "Loaded location - Device: " + deviceId +
                                            ", Lat: " + latitude + ", Lng: " + longitude);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing location document: " + e.getMessage());
                        }
                    }

                    loadingIndicator.setVisibility(View.GONE);

                    // Update UI with count
                    waitlistCountText.setText("Waitlist: " + queryDocumentSnapshots.size() + " users");

                    if (validLocationCount > 0) {
                        displayMarkersOnMap();

                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setMessage("Loaded " + validLocationCount + " locations. Tap markers for user details.")
                                .setPositiveButton("OK", null)
                                .show();

                    } else {

                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setMessage("No location data available for this event.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading locations: " + e.getMessage());
                    Toast.makeText(this,
                            "Error loading locations: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays all entrant locations as markers on the map
     * and adjusts camera to show all markers
     */
    private void displayMarkersOnMap() {
        if (mMap == null || entrantLocations.isEmpty()) {
            return;
        }

        // Clear existing markers and mapping
        mMap.clear();
        markerToDeviceIdMap.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int markerCount = 0;

        // Add a marker for each entrant location
        for (int i = 0; i < entrantLocations.size(); i++) {
            EntrantLocation location = entrantLocations.get(i);
            LatLng position = new LatLng(location.latitude, location.longitude);

            // Create marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title("Entrant " + (i + 1))
                    .snippet("Tap for user details")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            Marker marker = mMap.addMarker(markerOptions);

            // Map marker ID to device ID for click handling
            if (marker != null) {
                markerToDeviceIdMap.put(marker.getId(), location.deviceId);
            }

            boundsBuilder.include(position);
            markerCount++;
        }

        // Adjust camera to show all markers
        if (markerCount > 0) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100; // padding in pixels

                // Animate camera to show all markers
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera: " + e.getMessage());
            }
        }

        Log.d(TAG, "Displayed " + markerCount + " markers on map");
    }

    /**
     * Fetches and displays user details from the Users collection
     * @param deviceId The device ID to look up user information
     */
    private void fetchAndDisplayUserDetails(String deviceId) {
        loadingIndicator.setVisibility(View.VISIBLE);

        // Query the Users collection using deviceId
        db.collection("Profiles")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingIndicator.setVisibility(View.GONE);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first matching user document
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);

                        // Extract user details
                        String name = userDoc.getString("name");
                        String email = userDoc.getString("email");
                        String phone = userDoc.getString("phone");
                        String profileImageUrl = userDoc.getString("profileImageUrl");

                        // Build details message
                        StringBuilder detailsBuilder = new StringBuilder();
                        detailsBuilder.append("User Details:\n\n");

                        if (name != null && !name.isEmpty()) {
                            detailsBuilder.append("Name: ").append(name).append("\n");
                        }
                        if (email != null && !email.isEmpty()) {
                            detailsBuilder.append("Email: ").append(email).append("\n");
                        }
                        if (phone != null && !phone.isEmpty()) {
                            detailsBuilder.append("Phone: ").append(phone).append("\n");
                        }

                        detailsBuilder.append("\nDevice ID: ").append(deviceId);

                        // Show details in dialog
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Entrant Information")
                                .setMessage(detailsBuilder.toString())
                                .setPositiveButton("OK", null)
                                .show();

                    } else {
                        // No user found with this device ID
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("User Not Found")
                                .setMessage("No user information available for this device.\n\nDevice ID: " + deviceId)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching user details: " + e.getMessage());
                    Toast.makeText(this,
                            "Error loading user details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Inner class to represent an entrant's registration location
     */
    private static class EntrantLocation {
        String deviceId;
        double latitude;
        double longitude;

        EntrantLocation(String deviceId, double latitude, double longitude) {
            this.deviceId = deviceId;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}