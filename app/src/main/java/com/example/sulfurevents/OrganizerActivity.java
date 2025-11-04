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






}