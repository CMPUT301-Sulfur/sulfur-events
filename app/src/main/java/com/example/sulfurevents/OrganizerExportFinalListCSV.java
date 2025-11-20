
package com.example.sulfurevents;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

public class OrganizerExportFinalListCSV extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private EditText emailInput;
    private EditText descriptionInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_export_final_list_csv_activity);

        db = FirebaseFirestore.getInstance();
        emailInput = findViewById(R.id.organizerEmailCSV);
        descriptionInput = findViewById(R.id.etExportDescription);


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

        // button to export
        Button ExportCSVbtn = findViewById(R.id.btnExportCSV);
        ExportCSVbtn.setOnClickListener(v ->{
            GetDescription();
        });
    }

    private void GetDescription(){
        String description = descriptionInput.getText().toString().trim();

        // If description is empty use default messgae
        if(description.isEmpty()){

            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(document ->{
                        if(document.exists()){
                            String EventTitle = document.getString("eventName");
                            if(EventTitle != null && !EventTitle.isEmpty()){
                                // jump to the export function
                                ExportFinalList(description, EventTitle);
                            }else{
                                Toast.makeText(this, "Event title not found", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->{
                        Toast.makeText(this, "Error fetching event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }

    }

    private void ExportFinalList(String Description, String EventTitle){
        // Fetch the enrolled list
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(document ->{
                    if(!document.exists()){
                        Toast.makeText(this, "No participants in final list", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Collect all device ids
                    java.util.List<String> deviceIds = (java.util.List<String>) document.get("enrolled_list");

                    if(deviceIds == null || deviceIds.isEmpty()){
                        Toast.makeText(this, "No participants in final list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    android.util.Log.d("CSV_DEBUG", "Device IDs found: " + deviceIds.size());

                    fetchProfiles(deviceIds,Description, EventTitle);

                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Error fetching list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProfiles(java.util.List<String> deviceIds, String Description, String EventTitle){
        if (deviceIds.isEmpty()) {
            Toast.makeText(this, "No device IDs found", Toast.LENGTH_SHORT).show();
            return;
        }

        // get the profiles taht are linked to each device id
        db.collection("Profiles").whereIn("deviceId", deviceIds)
                .get()
                .addOnSuccessListener(profileSnapshot ->{
                    if(profileSnapshot.isEmpty()){
                        Toast.makeText(this, "No matching profiles found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // format as csv
                    String CSVData = formatCSV(profileSnapshot);

                    String fileName = EventTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_Final_List.csv";
                    saveCSVToDownloads(fileName, CSVData);



                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Error fetching list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Two smaller helper functions
    private String formatCSV(QuerySnapshot querySnapshot){
        StringBuilder csv = new StringBuilder();

        // Header Row
        csv.append("Name, Email, Phone Number\n");

        // Data rows
        for (QueryDocumentSnapshot doc : querySnapshot){
            String name = doc.getString("name") != null ? doc.getString("name") : "";
            String email = doc.getString("email") != null ? doc.getString("email") : "";
            String phone = doc.getString("phone") != null ? doc.getString("phone") : "";

            csv.append(name).append(",")
                    .append(email).append(",")
                    .append(phone).append("\n");
        }


        return csv.toString();

    }



    private void saveCSVToDownloads(String fileName, String csvData) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(csvData.getBytes());
                        outputStream.close();
                        Toast.makeText(this, "CSV saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

                        // Offering to user to  open/share the file
                        offerToOpenFile(uri, fileName);
                    }
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);

                FileWriter writer = new FileWriter(file);
                writer.write(csvData);
                writer.close();

                Toast.makeText(this, "CSV saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

                // For older Android versions, create URI using FileProvider
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        file
                );
                offerToOpenFile(uri, fileName);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void offerToOpenFile(android.net.Uri uri, String fileName) {
        // Create dialog to ask user what they want to do
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("CSV Exported Successfully")
                .setMessage("File saved as: " + fileName + "\n\nWhat would you like to do?")
                .setPositiveButton("Open", (dialog, which) -> {
                    openFile(uri);
                })
                .setNegativeButton("Share", (dialog, which) -> {
                    shareFile(uri, fileName);
                })
                .setNeutralButton("Done", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void openFile(android.net.Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open CSV with..."));
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open CSV files. Try sharing instead.", Toast.LENGTH_LONG).show();
        }
    }

    private void shareFile(android.net.Uri uri, String fileName) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, fileName);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share CSV via..."));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}

