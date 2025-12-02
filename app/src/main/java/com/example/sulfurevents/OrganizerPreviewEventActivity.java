package com.example.sulfurevents;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for organizers to preview event details and QR code.
 * This activity can be launched in two ways:
 * 1. Direct navigation with an EVENT_ID extra
 * 2. Deep link from a URL (e.g., sulfurevents://event/abc123)
 *
 * The activity displays the event's QR code retrieved from Firestore,
 * which can be used for event check-in or sharing.
 */
public class OrganizerPreviewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    /**
     * Called when the activity is created.
     * Handles both deep link navigation and normal intent navigation to display
     * the event QR code and event ID.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_preview_event);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Get references to UI components
        ImageView qrImage = findViewById(R.id.qrImage);
        TextView eventIdText = findViewById(R.id.eventIdText);
        ImageButton back = findViewById(R.id.backButton);

        // Set up back button to close the activity
        back.setOnClickListener(v -> finish());

        // Get the intent that started this activity
        Intent intent = getIntent();

        // ---------- HANDLE DEEP LINK FIRST ----------
        // Check if the activity was launched via a deep link URL
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();   // e.g., sulfurevents://event/abc123

            if (uri != null) {
                // Extract event ID from the last segment of the URI
                String eventId = uri.getLastPathSegment();  // "abc123"
                eventIdText.setText("Event ID: " + eventId);
                loadEvent(eventId, qrImage);
                return;
            }
        }

        // ---------- NORMAL NAVIGATION ----------
        // Handle standard intent navigation with EVENT_ID extra
        String eventId = intent.getStringExtra("EVENT_ID");

        if (eventId != null) {
            eventIdText.setText("Event ID: " + eventId);
            loadEvent(eventId, qrImage);
        }
    }

    /**
     * Loads event data from Firestore and displays the QR code.
     * The QR code is stored as a Base64-encoded string in Firestore and is
     * decoded into a Bitmap for display.
     *
     * @param eventId The ID of the event to load
     * @param qrImage The ImageView where the QR code will be displayed
     */
    private void loadEvent(String eventId, ImageView qrImage) {

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Retrieve the Base64-encoded QR code string
                        String base64QR = doc.getString("qrCode");

                        if (base64QR != null) {
                            // Decode Base64 string to byte array
                            byte[] bytes = Base64.decode(base64QR, Base64.DEFAULT);

                            // Convert byte array to Bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Display the QR code in the ImageView
                            qrImage.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error if Firestore query fails
                    e.printStackTrace();
                });
    }
}