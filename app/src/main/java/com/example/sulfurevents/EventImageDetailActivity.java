// EventImageDetailActivity
// Displays the selected event’s image and lets the admin delete it from Firebase Storage and Firestore.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This class defines the event image detail screen.
 * It lets administrators view and delete an event's uploaded image.
 */
public class EventImageDetailActivity extends AppCompatActivity {

    private ImageView imgEvent;
    private TextView tvEventTitle, tvEventInfo;
    private Button btnBack, btnDelete;
    private String eventId, eventName, organizerEmail, posterURL;

    /**
     * Called when the activity is created.
     * Displays the event image and sets up delete and back buttons.
     * @param savedInstanceState The saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_image_detail_activity);

        // UI references
        imgEvent = findViewById(R.id.imgEventDetail);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventInfo = findViewById(R.id.tvEventInfo);

        ImageButton btnBackTop = findViewById(R.id.btnBackEventDetail);
        Button btnBackBottom = findViewById(R.id.btnBackEventDetailBottom);
        btnDelete = findViewById(R.id.btnDeleteImage);

        // Get Intent data
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        organizerEmail = getIntent().getStringExtra("organizerEmail");
        posterURL = getIntent().getStringExtra("posterURL");

        if (eventName == null) eventName = "Unknown Event";
        if (organizerEmail == null) organizerEmail = "Unknown Organizer";

        tvEventTitle.setText(eventName);
        tvEventInfo.setText("Organizer: " + organizerEmail);

        Glide.with(this)
                .load(posterURL)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgEvent);

        // Back buttons
        btnBackTop.setOnClickListener(v -> finish());
        btnBackBottom.setOnClickListener(v -> finish());

        // Delete button
        btnDelete.setOnClickListener(v -> deleteImageFromFirebase());
    }


    /**
     * Deletes the event image from Firebase Storage and updates Firestore.
     */
    private void deleteImageFromFirebase() {
        if (posterURL == null || posterURL.isEmpty()) {
            Toast.makeText(this, "No image URL found.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(posterURL);
            photoRef.delete().addOnSuccessListener(aVoid -> {
                FirebaseFirestore.getInstance()
                        .collection("Events")
                        .document(eventId)
                        .update("posterURL", "")
                        .addOnSuccessListener(a -> {
                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Image Deleted")
                                    .setMessage("Image deleted successfully.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to delete from Storage: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error: invalid image URL or missing storage reference.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}