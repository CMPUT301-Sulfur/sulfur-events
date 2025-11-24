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

public class OrganizerPreviewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_preview_event);

        db = FirebaseFirestore.getInstance();

        ImageView qrImage = findViewById(R.id.qrImage);
        TextView eventIdText = findViewById(R.id.eventIdText);
        ImageButton back = findViewById(R.id.backButton);

        back.setOnClickListener(v -> finish());

        // ---------- HANDLE DEEP LINK FIRST ----------
        Intent intent = getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();   // e.g., sulfurevents://event/abc123

            if (uri != null) {
                String eventId = uri.getLastPathSegment();  // "abc123"
                eventIdText.setText("Event ID: " + eventId);
                loadEvent(eventId, qrImage);
                return;
            }
        }

        // ---------- NORMAL NAVIGATION ----------
        String eventId = intent.getStringExtra("EVENT_ID");

        if (eventId != null) {
            eventIdText.setText("Event ID: " + eventId);
            loadEvent(eventId, qrImage);
        }
    }

    private void loadEvent(String eventId, ImageView qrImage) {

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String base64QR = doc.getString("qrCode");

                        if (base64QR != null) {
                            byte[] bytes = Base64.decode(base64QR, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            qrImage.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Optional: show error message or log
                    e.printStackTrace();
                });
    }
}
