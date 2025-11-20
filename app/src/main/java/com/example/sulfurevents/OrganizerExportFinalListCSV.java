
package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerExportFinalListCSV extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_export_final_list_csv_activity);

        db = FirebaseFirestore.getInstance();

        // back button to go back
        ImageButton backbutton = findViewById(R.id.btnBack);
        backbutton.setOnClickListener(v -> {
            finish(); // Go back to previous screen
        });
    }







}
