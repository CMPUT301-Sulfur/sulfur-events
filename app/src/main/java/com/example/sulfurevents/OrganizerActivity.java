package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

        OrganizerEventsAdapter adapter = new OrganizerEventsAdapter(this, OrganizerEvent);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        DeviceID = Settings.Secure
                .getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);


        // we are displaying events
        db = FirebaseFirestore.getInstance();

        db.collection("Events").whereEqualTo("organizerId", DeviceID)
                .addSnapshotListener((value, error) ->{
                    if(error != null || value == null){
                        return;
                    }else{
                        OrganizerEvent.clear();
                        OrganizerEvent.addAll(value.toObjects(OrganizerEvent.class));
                        adapter.notifyDataSetChanged();
                    }
                });


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

//        Button editEventButton = findViewById(R.id.EditEventButtonCard);
//        editEventButton.setOnClickListener(view ->{
//            Intent intent = new Intent(OrganizerActivity.this, OrganizerEditEventActivity.class);
//            startActivity(intent);
//        });

    }






}