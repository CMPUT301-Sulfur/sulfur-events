package com.example.sulfurevents;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * The {@code OrganizerCreateEventActivity} class allows organizers to create new events
 * in the SulfurEvents application.
 * <p>
 * This screen lets the user:
 * <ul>
 *     <li>Enter event details (name, description, date, etc.)</li>
 *     <li>Upload an event poster image</li>
 *     <li>Automatically generate a QR code for the event</li>
 * </ul>
 *
 * The new event is stored in the Firestore "Events" collection.
 *
 * <p>Associated layout: {@code create_event_activity.xml}
 */
public class OrganizerCreateEventActivity extends AppCompatActivity {

    /** Request code for selecting an image from the gallery. */
    private static final int IMAGE_REQUEST = 1;

    /** URI of the uploaded event poster image. */
    private Uri posterUri = null;

    /** Firestore database instance. */
    private FirebaseFirestore db;

    /** Device ID used as the organizer’s unique identifier. */
    private String DeviceID;

    /** The currently logged-in user (if available). */
    private User CurrentUser;


    /**
     * Called when the activity is first created.
     * <p>
     * Sets up UI elements, initializes Firebase, and configures button click listeners
     * for creating events and uploading poster images.
     *
     * @param savedInstanceState The saved instance state bundle, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_event_activity);

        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.GenerateEventButton), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // getting the instance of device id and database access
        db = FirebaseFirestore.getInstance();
        DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);



        // Back Button to main organizer home page with list of events
        ImageButton backButton = findViewById(R.id.Back_Button);
        backButton.setOnClickListener(v -> finish());

        // Generate Link and Add Event Button
        Button GenerateQRCodeEventButon = findViewById(R.id.GenerateEventButton);
        GenerateQRCodeEventButon.setOnClickListener(view ->{
            // CreateEvent(String Device ID, User CurrentUser,);
            CreateEvent();
        });

        // listen for user to click on upload poster area
        FrameLayout poster = findViewById(R.id.posterUploadArea);
        poster.setOnClickListener(view ->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,IMAGE_REQUEST);
        });

        // listen for user to click the date section on the date buttons
        EditText start = findViewById(R.id.etStartDate);
        EditText end = findViewById(R.id.etEndDate);

        setdate(start);
        setdate(end);

    }



    /**
     * Generates a QR code bitmap for the given value.
     *
     * @param value The text or ID to encode in the QR code.
     * @return A Bitmap containing the generated QR code.
     * @throws Exception If QR code generation fails.
     */
    private Bitmap generateQR(String value) throws Exception{
        com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
        return encoder.encodeBitmap(value, com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
    }

    /**
     * Converts a Bitmap image to a Base64-encoded string.
     *
     * @param bitmap The bitmap to convert.
     * @return A Base64 string representing the bitmap.
     */
    private String bitmaptobase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Creates a new event and uploads its data to Firestore.
     * <p>
     * If an image is selected, it uploads the event poster to Firebase Storage
     * and stores the download URL in the event document.
     * A unique event ID is generated for each event.
     */
    private void CreateEvent() {
        String title = ((EditText)findViewById(R.id.etEventName)).getText().toString();
        String description = ((EditText)findViewById(R.id.etDescription)).getText().toString();

        String start = ((EditText)findViewById(R.id.etStartDate)).getText().toString();
        String end = ((EditText)findViewById(R.id.etEndDate)).getText().toString();


        String location = ((EditText)findViewById(R.id.etLocation)).getText().toString();
        String limit = ((EditText)findViewById(R.id.etLimitGuests)).getText().toString();
        String OGEmail = ((EditText)findViewById(R.id.organizerEmail)).getText().toString();


        // Generate deep link + QR
        Bitmap qrBitmap;
        String qrBase64;
        try {
            String deepLink = "sulfurevents://event/" + eventId;
            qrBitmap = generateQR(deepLink);
            qrBase64 = bitmaptobase64(qrBitmap);
        } catch (Exception e) {
            Toast.makeText(this, "QR creation failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isBlank() || description.isBlank() || start.isBlank() ||
                end.isBlank() || location.isBlank() || limit.isBlank() || OGEmail.isBlank()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        OrganizerEvent event = new OrganizerEvent();
        event.eventId = eventId;
        event.organizerId = DeviceID;
        event.eventName = title;
        event.description = description;
        event.startDate = start;
        event.endDate = end;
        event.location = location;
        event.limitGuests = limit;
        event.qrCode = qrBase64;
        event.organizerEmail = OGEmail;

        // Save poster
        if (posterUri == null) {
            event.posterURL = null;

            db.collection("Events").document(eventId)
                    .set(event)
                    .addOnSuccessListener(unused -> {

                        // SAVE QR TO GALLERY HERE
                        saveQRToGallery(qrBitmap, eventId);

                        // FINISH BACK TO ORGANIZER HOME
                        finish();
                    });

        } else {
            StorageReference storeref = FirebaseStorage.getInstance()
                    .getReference("Event_Posters")
                    .child(eventId + ".jpg");

            storeref.putFile(posterUri).addOnSuccessListener(task -> {
                storeref.getDownloadUrl().addOnSuccessListener(downloadURl -> {
                    event.posterURL = downloadURl.toString();

                    db.collection("Events").document(eventId)
                            .set(event)
                            .addOnSuccessListener(unused -> {

                                // SAVE QR TO GALLERY HERE
                                saveQRToGallery(qrBitmap, eventId);

                                // FINISH BACK TO ORGANIZER HOME
                                finish();
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Poster Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void saveQRToGallery(Bitmap bitmap, String eventId) {
        OutputStream fos;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "event_qr_" + eventId + ".png");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/SulfurEvents");

                Uri imageUri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                );
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagesDir =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        ).toString() + "/SulfurEvents";

                File dir = new File(imagesDir);
                if (!dir.exists()) dir.mkdirs();

                File image = new File(dir, "event_qr_" + eventId + ".png");
                fos = new FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(this, "QR saved to gallery!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Handles the result from the image picker intent.
     * <p>
     * Displays a preview of the selected event poster or resets to default
     * if no image is chosen.
     *
     * @param requestCode The request code used when starting the activity.
     * @param resultCode  The result code returned by the activity.
     * @param data        The intent data containing the selected image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        ImageView eventposter = findViewById(R.id.eventPosterPreview);

        if(requestCode == IMAGE_REQUEST && resultCode != RESULT_OK){
            posterUri = null;
            eventposter.setImageResource(R.drawable.upload); // back to default icon
            return;
        }

        if(requestCode  == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            posterUri = data.getData();
            eventposter.setImageURI(posterUri);
        }
    }

    private void setdate(EditText editText){
        editText.setOnClickListener(v ->{
            Calendar calendar = Calendar.getInstance();
            // if there is already a date parse the date
            String CurrentDate = editText.getText().toString();
            if(!CurrentDate.isBlank() && CurrentDate.length() == 10){
                try{
                    String parts[] = CurrentDate.split("/");
                    int month = Integer.parseInt(parts[0]) - 1;
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }catch (Exception e){

                }
            }


            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%04d",
                                selectedMonth + 1, // Add 1 because month is 0-indexed
                                selectedDay,
                                selectedYear);
                        editText.setText(formattedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private boolean isDateValid(String start, String end){
        if(start.isEmpty() || end.isEmpty()){
            return false;
        }
        String[] StartSplit = start.split("/");
        String[] EndSplit = end.split("/");


        // Year
        Integer Syear = Integer.parseInt(StartSplit[2]);
        Integer Eyear = Integer.parseInt(EndSplit[2]);
        //day
        Integer Sday = Integer.parseInt(StartSplit[1]);
        Integer Eday = Integer.parseInt(EndSplit[1]);
        // month
        Integer Smonth = Integer.parseInt(StartSplit[0]);
        Integer Emonth = Integer.parseInt(EndSplit[0]);
        // year comparison
        if(Eyear > Syear) return true;
        if(Eyear < Syear) return false;

        // month comparison
        if(Emonth > Smonth) return true;
        if(Emonth < Smonth) return false;

        // compare day
        return Eday >= Sday;
    }

}
