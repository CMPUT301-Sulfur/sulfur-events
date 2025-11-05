package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private String DeviceID;
    private User CurrentUser;

    ArrayList<OrganizerEvent> OrganizerEvent = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.organizer_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.OrganizerEventView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.CreatedEventsRecyclerView);


//        OrganizerEvent.add(new Event(
//                "Test",
//                "OCT, 2004",
//                "Canada",
//                15
//        ));

        OrganizerEventsAdapter adapter = new OrganizerEventsAdapter(this, OrganizerEvent);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Back Button back into mainActivity
        ImageButton BackButton = findViewById(R.id.BackButtonOrganizerEvents);
        BackButton.setOnClickListener(view ->{
            Intent intent = new Intent(OrganizerActivity.this, MainActivity.class);
            finish();
        });

        // Create Event button
        Button createEventButton = findViewById(R.id.CreateEventButton);
        createEventButton.setOnClickListener(view ->{
            Intent intent = new Intent(OrganizerActivity.this, OrganizerCreateEventActivity.class);
            startActivity(intent);
        });
    }
    private void drawWinners(String eventId, int numberToPick) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events").document(eventId)
                .collection("WaitingList")
                .get()
                .addOnSuccessListener(qs -> {
                    java.util.List<com.google.firebase.firestore.DocumentSnapshot> all = qs.getDocuments();
                    java.util.Collections.shuffle(all); // random order
                    int toPick = Math.min(numberToPick, all.size());

                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (int i = 0; i < all.size(); i++) {
                        com.google.firebase.firestore.DocumentSnapshot ds = all.get(i);
                        String profileId = ds.getId(); // assuming doc id == profileId

                        if (i < toPick) {
                            // chosen
                            batch.update(ds.getReference(), "status", "chosen");

                            String notifId = db.collection("Notifications").document().getId();
                            java.util.Map<String, Object> notif = new java.util.HashMap<>();
                            notif.put("toProfileId", profileId);
                            notif.put("eventId", eventId);
                            notif.put("type", "chosen");
                            notif.put("message", "You were selected for this event. Accept or decline.");
                            notif.put("createdAt", com.google.firebase.Timestamp.now());
                            batch.set(db.collection("Notifications").document(notifId), notif);

                        } else {
                            // not chosen
                            batch.update(ds.getReference(), "status", "not_chosen");

                            String notifId = db.collection("Notifications").document().getId();
                            java.util.Map<String, Object> notif = new java.util.HashMap<>();
                            notif.put("toProfileId", profileId);
                            notif.put("eventId", eventId);
                            notif.put("type", "not_chosen");
                            notif.put("message", "You were not selected in the first draw.");
                            notif.put("createdAt", com.google.firebase.Timestamp.now());
                            batch.set(db.collection("Notifications").document(notifId), notif);
                        }
                    }

                    batch.commit();
                });
    }







}