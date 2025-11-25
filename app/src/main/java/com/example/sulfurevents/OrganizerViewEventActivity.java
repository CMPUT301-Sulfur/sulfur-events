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
        //tvEmail.setText("Organizer: " + event.getOrganizerEmail());
        db.collection("Profiles").document(event.organizerId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String email = d.getString("email");
                        tvEmail.setText("Organizer: " + email);
                    } else {
                        tvEmail.setText("Organizer: Unknown");
                    }
                });


        tvEmail.setTag(event.getPosterURL());


    }

    // Export to CSV file Logic

    /**
     * Initiates the process of exporting a CSV file for a specific event.
     *
     * <p>This method retrieves the event document from the "Events" collection in Firestore
     * using the provided {@code eventId}. If the document exists and contains a valid
     * {@code eventName}, the method proceeds by calling {@link #ExportFinalList(String)}
     * to complete the CSV export process.</p>
     *
     * <p>If the event does not exist, the event name is missing, or an error occurs during
     * Firestore retrieval, Toast messages are displayed to the user.</p>
     *
     * <p>Error scenarios handled:
     * <ul>
     *     <li>Event document not found</li>
     *     <li>Event title not found or empty</li>
     *     <li>General Firestore retrieval failure</li>
     * </ul>
     * </p>
     */
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

    /**
     * Generates and exports the final participant list for a given event as a CSV file.
     *
     * <p>This method retrieves the event document from Firestore using the provided
     * {@code eventId}. If the event exists and contains an {@code enrolled_list} field,
     * the method extracts the device IDs of enrolled participants. It then delegates
     * to {@link #fetchProfiles(java.util.List, String)} to resolve participant profiles
     * and complete the CSV export.</p>
     *
     * <p>Behavior:
     * <ul>
     *   <li>Displays a notification if the event document does not exist.</li>
     *   <li>Validates that the {@code enrolled_list} field is present and non-empty.</li>
     *   <li>Logs the number of retrieved device IDs for debugging.</li>
     *   <li>Handles Firestore lookup failures and reports them via Toast messages.</li>
     * </ul>
     * </p>
     *
     * @param EventTitle The title of the event used to name or label the output CSV file.
     */
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

    /**
     * Retrieves participant profiles linked to the provided device IDs, formats the
     * results into a CSV structure, and saves the generated CSV file to the user's
     * Downloads directory.
     *
     * <p>This method performs a Firestore query against the {@code Profiles} collection,
     * filtering by the list of {@code deviceIds}. If matching profiles are found, they
     * are converted into CSV format through {@link #formatCSV(com.google.firebase.firestore.QuerySnapshot)}
     * and written to local storage using {@code saveCSVToDownloads}.</p>
     *
     * <p>Behavior:
     * <ul>
     *   <li>Validates that the provided device ID list is non-empty.</li>
     *   <li>Queries Firestore for profiles whose {@code deviceId} field matches the IDs.</li>
     *   <li>Handles cases where no profiles are found.</li>
     *   <li>Constructs a sanitized CSV file name derived from {@code EventTitle}.</li>
     *   <li>Persists the CSV file to the device download directory.</li>
     *   <li>Reports all failure conditions via Toast messages.</li>
     * </ul>
     * </p>
     *
     * @param deviceIds  List of device identifiers used to match participant profiles.
     * @param EventTitle Title of the event, used to generate the output CSV file name.
     */
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

    /**
     * Fetches participant profiles associated with the specified device IDs, converts the
     * resulting data into CSV format, and exports the CSV file to the user's Downloads folder.
     *
     * <p>The method queries the Firestore {@code Profiles} collection for documents whose
     * {@code deviceId} field matches any ID in the provided list. When profiles are found,
     * the data is passed to {@link #formatCSV(com.google.firebase.firestore.QuerySnapshot)}
     * for CSV construction and then saved locally via {@code saveCSVToDownloads}.</p>
     *
     * <p>Operational details:
     * <ul>
     *   <li>Ensures the list of device IDs is not empty before proceeding.</li>
     *   <li>Performs a Firestore {@code whereIn} lookup using the given device IDs.</li>
     *   <li>Validates that at least one matching profile is returned.</li>
     *   <li>Generates a sanitized filename based on {@code EventTitle}.</li>
     *   <li>Saves the resulting CSV file into the device’s default Downloads directory.</li>
     *   <li>Displays Toast notifications for all error or empty-result conditions.</li>
     * </ul>
     * </p>
     */
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


    /**
     * Saves the generated CSV data to the device's Downloads directory and optionally
     * prompts the user to open or share the resulting file.
     *
     * <p>The method supports both modern (Android 10 / API 29+) and legacy Android
     * storage behavior. On Android Q and newer, it uses the MediaStore API to insert
     * the file directly into the system-managed Downloads collection. On older versions,
     * it writes to the public Downloads directory using traditional file I/O and exposes
     * the file through a {@code FileProvider}.</p>
     *
     * <p>Operational details:
     * <ul>
     *   <li>Creates a CSV file named according to {@code fileName}.</li>
     *   <li>Writes {@code csvData} to the output file as UTF-8 bytes.</li>
     *   <li>Displays a Toast notification confirming success or failure.</li>
     *   <li>Invokes {@code offerToOpenFile} to let the user open or share the file.</li>
     *   <li>Handles all storage operations in a try/catch block to prevent crashes.</li>
     * </ul>
     * </p>
     *
     * <p>Android Q+ specifics:
     * <ul>
     *   <li>Uses {@code MediaStore.Downloads} with {@code RELATIVE_PATH} to avoid requiring storage permissions.</li>
     *   <li>Obtains a managed Uri and writes using {@link android.content.ContentResolver#openOutputStream}.</li>
     * </ul>
     * </p>
     *
     * <p>Legacy (Android 9 and below):
     * <ul>
     *   <li>Writes directly into the public Downloads directory via {@code FileWriter}.</li>
     *   <li>Wraps the file with a {@code FileProvider} to maintain secure URI access.</li>
     * </ul>
     * </p>
     *
     * @param fileName The desired name of the CSV file, including the .csv extension.
     */

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

    /**
     * Presents a modal dialog offering actions the user can take after a CSV file
     * has been successfully exported. The dialog provides options to open the file,
     * share it, or dismiss the prompt.
     *
     * <p>The method displays an {@link androidx.appcompat.app.AlertDialog} containing
     * three user-selectable actions:
     * <ul>
     *   <li><strong>Open</strong> – Launches an appropriate viewer by calling {@link #openFile(android.net.Uri)}.</li>
     *   <li><strong>Share</strong> – Opens the system share sheet via {@link #shareFile(android.net.Uri, String)}.</li>
     *   <li><strong>Done</strong> – Closes the dialog without performing any additional action.</li>
     * </ul>
     * The dialog is non-cancelable, ensuring the user explicitly selects one of the options.</p>
     *
     * <p>The provided {@code uri} represents the location of the newly created CSV file,
     * and {@code fileName} is displayed to the user so they can identify the exported file.</p>
     *
     * @param uri       The content URI of the exported CSV file.
     */

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


    /**
     * Shares the exported CSV file using the system's share sheet. This method constructs
     * an {@link Intent#ACTION_SEND} intent, attaches the provided file URI, and grants
     * temporary read permission to the receiving application.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Sets the MIME type to {@code text/csv} so compatible apps can handle it.</li>
     *   <li>Includes the file URI via {@link Intent#EXTRA_STREAM}.</li>
     *   <li>Sets the file name in {@link Intent#EXTRA_SUBJECT} for better context in receiving apps.</li>
     *   <li>Uses {@code FLAG_GRANT_READ_URI_PERMISSION} to allow external apps to access the file.</li>
     *   <li>Launches a chooser dialog so the user can select how to share the file.</li>
     *   <li>Catches and reports any exceptions with a Toast message.</li>
     * </ul>
     * </p>
     *
     * @param uri       The content URI pointing to the CSV file to be shared.
     * @param fileName  The name of the CSV file, used as the share subject.
     */

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
