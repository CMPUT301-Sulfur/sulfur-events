// EventImageDetailActivity
// Displays the selected event’s image and lets the admin delete it from Firebase Storage and Firestore.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EventImageDetailActivity extends AppCompatActivity {

    private ImageView imgEvent;
    private TextView tvEventTitle, tvEventInfo;
    private Button btnBack, btnDelete;
    private String eventId, eventName, organizerEmail, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_image_detail_activity);

        // bind layout views
        imgEvent = findViewById(R.id.imgEventDetail);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        btnBack = findViewById(R.id.btnBackEventDetail);
        btnDelete = findViewById(R.id.btnDeleteImage);

        // retrieve event data from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        organizerEmail = getIntent().getStringExtra("organizerEmail");
        imageUrl = getIntent().getStringExtra("imageUrl");

        // prevent null crashes
        if (eventName == null) eventName = "Unknown Event";
        if (organizerEmail == null) organizerEmail = "Unknown Organizer";

        tvEventTitle.setText(eventName);
        tvEventInfo.setText("Organizer: " + organizerEmail);

        // load the image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgEvent);

        // back button
        btnBack.setOnClickListener(v -> finish());

        // delete button
        btnDelete.setOnClickListener(v -> deleteImageFromFirebase());
    }

    private void deleteImageFromFirebase() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "No image URL found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // firebase Storage deletion
        try {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            photoRef.delete().addOnSuccessListener(aVoid -> {
                // Update Firestore: clear imageUrl
                FirebaseFirestore.getInstance()
                        .collection("Events")
                        .document(eventId)
                        .update("imageUrl", "")
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
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
