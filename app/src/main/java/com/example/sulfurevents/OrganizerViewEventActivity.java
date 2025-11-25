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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;


// Java docs for part 3 finished 
/**
 * Displays detailed information about a specific event.
 * Includes a back button to return to the "Your Events" list.
 */
public class OrganizerViewEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvEventName, tvDescription, tvStartDate, tvEndDate, tvLocation, tvCapacity, tvEmail;
    private ImageButton backButton;

    private EditText emailInput;
    private EditText descriptionInput;
    private String eventId;



    /**
     * Displays the details of a selected event and provides navigation
     * to view waitlisted, enrolled, and invited user lists.
     *
     * @param savedInstanceState Previous activity state if being restored.
     *
     * Steps:
     * <ul>
     *   <li>Retrieves the event ID passed from the previous screen</li>
     *   <li>Initializes and binds UI components</li>
     *   <li>Loads event details from Firestore and displays them</li>
     *   <li>Configures navigation to Waitlist, Enrolled, and Invited screens</li>
     *   <li>Back button returns the user to the Organizer home screen</li>
     * </ul>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_view_event_activity);

        db = FirebaseFirestore.getInstance();

        // get event id from intent
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // initialize UI
        backButton = findViewById(R.id.backButtonEventDetails);
        tvEventName = findViewById(R.id.tvEventName);
        tvDescription = findViewById(R.id.tvDescription);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvEmail = findViewById(R.id.tvEmail);


        //  Back button logic
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerViewEventActivity.this, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // load event details
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(this::populateEvent)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                    finish();
                });

        Button btnViewWaitingList = findViewById(R.id.btnViewWaitingList);
        btnViewWaitingList.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerWaitlistActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        Button btnViewEnrolled = findViewById(R.id.btnViewEnrolled);
        btnViewEnrolled.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerEnrolledActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        Button btnViewInvited = findViewById(R.id.btnViewInvited);
        btnViewInvited.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerInvitedActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        Button btnViewCancelled = findViewById(R.id.btnViewCancelled);
        btnViewCancelled.setOnClickListener(v -> {
            Intent i = new Intent(OrganizerViewEventActivity.this, OrganizerCancelledActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });



        // Export Final List in CSV format file
        Button btnExportFinalListCSV = findViewById(R.id.btnExportFinalListCSV);
        btnExportFinalListCSV.setOnClickListener(v ->{
            ExportCSVFile();
        });


        Button btnEditEvent = findViewById(R.id.EditEventButton);
        btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerViewEventActivity.this, OrganizerCreateEventActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", tvEventName.getText().toString());
            intent.putExtra("description", tvDescription.getText().toString());
            intent.putExtra("startDate", tvStartDate.getText().toString().replace("Start Date: ", ""));
            intent.putExtra("endDate", tvEndDate.getText().toString().replace("End Date: ", ""));
            intent.putExtra("location", tvLocation.getText().toString().replace("Location: ", ""));
            intent.putExtra("capacity", tvCapacity.getText().toString().replace("Capacity: ", ""));
            intent.putExtra("organizerEmail", tvEmail.getText().toString().replace("Organizer: ", ""));
            intent.putExtra("posterURL", (String) tvEmail.getTag());
            startActivity(intent);
        });


    }


    /**
     * Populates the event details screen using data from the retrieved Firestore document.
     *
     * @param doc The Firestore document containing event data.
     *
     * If the document does not exist, the activity closes. Otherwise, the event fields
     * are mapped and displayed in the corresponding TextViews.
     */
    private void populateEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        OrganizerEvent event = doc.toObject(OrganizerEvent.class);
        if (event == null) return;

        // fill text views with event data
        tvEventName.setText(event.getEventName());
        tvDescription.setText(event.getDescription());
        tvStartDate.setText("Start Date: " + event.getStartDate());
        tvEndDate.setText("End Date: " + event.getEndDate());
        tvLocation.setText("Location: " + event.getLocation());
        tvCapacity.setText("Capacity: " + event.getLimitGuests());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());
        tvEmail.setTag(event.getPosterURL());


    }

    // Export to CSV file Logic
    private void ExportCSVFile(){

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(document ->{
                    if(document.exists()){
                        String EventTitle = document.getString("eventName");
                        if(EventTitle != null && !EventTitle.isEmpty()){
                            // jump to the export function
                            ExportFinalList(EventTitle);
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

    private void ExportFinalList(String EventTitle){
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

                    fetchProfiles(deviceIds, EventTitle);

                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Error fetching list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProfiles(java.util.List<String> deviceIds, String EventTitle){
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


        // Format Row
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
                    // This Done button does nothing other than return you to the Edit event page
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
