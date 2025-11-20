
package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerExportFinalListCSV extends AppCompatActivity {

    private FirebaseFirestore db;
    String eventId

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

        // getting the userId for that event (ie. the userId that created that event)
        // get event id from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }

    private void GetEmail(){
        // Read the email from the text box
        String Email = ((EditText)findViewById(R.id.organizerEmailCSV)).getText().toString().trim();

        // If User does not input an email in the text box
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(Email.isEmpty()){
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(document ->{
                        if(document.exists()){
                            String email = document.getString("organizerEmail");
                        }
                    })

        }

    }




}
