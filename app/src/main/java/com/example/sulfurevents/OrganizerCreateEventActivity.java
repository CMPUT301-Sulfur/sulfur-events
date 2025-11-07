package com.example.sulfurevents;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

// Image constant

public class OrganizerCreateEventActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 1;
    private Uri posterUri = null;

    private FirebaseFirestore db;
    private String DeviceID;
    private User CurrentUser;

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

    }


    /**
     * Generates a QR code image from the given string value.
     *
     * @param value Text to encode into the QR code.
     * @return A 500x500 Bitmap containing the generated QR code.
     * @throws Exception If the encoding process fails.
     */
    // Generating QR code
    private Bitmap generateQR(String value) throws Exception{
        com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
        return encoder.encodeBitmap(value, com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
    }

    /**
     * Converts a Bitmap image into a Base64 encoded string.
     *
     * @param bitmap The Bitmap to convert.
     * @return A Base64 string representation of the Bitmap.
     */
    // Convert from bitmap to base 64 so the user is able to see which image they have uploaded
    private String bitmaptobase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }



    /**
     * Creates a new event using user input fields.
     * <p>
     * Steps:
     * <ul>
     *   <li>Reads event details from input fields</li>
     *   <li>Generates a unique event ID and QR code</li>
     *   <li>Validates required fields</li>
     *   <li>Saves the event to Firestore</li>
     *   <li>If a poster image was selected, uploads it to Firebase Storage
     *       and stores its URL in the event record</li>
     * </ul>
     * The activity finishes after a successful save.
     */
    private void CreateEvent(){
        String title = ((EditText)findViewById(R.id.etEventName)).getText().toString();
        String description = ((EditText)findViewById(R.id.etDescription)).getText().toString();
        String start = ((EditText)findViewById(R.id.etStartDate)).getText().toString();
        String end = ((EditText)findViewById(R.id.etEndDate)).getText().toString();
        String location = ((EditText)findViewById(R.id.etLocation)).getText().toString();
        String limit = ((EditText)findViewById(R.id.etLimitGuests)).getText().toString();
        String OGEmail = ((EditText)findViewById(R.id.organizerEmail)).getText().toString();

        String eventId = db.collection("Events").document().getId();

        Bitmap qrBitmap;
        String qrBase64;
        String Link;

        try{
            // assign returned bitmap
            qrBitmap = generateQR(eventId);
            // convert to base 64
            qrBase64 = bitmaptobase64(qrBitmap);
        }catch (Exception e){
            // we should add a toast and say "cannot create event"
            return;
        }


        if(title.isBlank() ||description.isBlank() ||start.isBlank() ||
        end.isBlank() || location.isBlank() || limit.isBlank() || OGEmail.isBlank()){
            Toast.makeText(this, "Please, fill all fields.", Toast.LENGTH_SHORT).show();
            return; // Stop here, stay on this screen
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

        // if the event poster is null just store null in the database for event poster
        if(posterUri == null){
            event.posterURL = null;
            db.collection("Events").document(eventId)
                    .set(event)
                    .addOnSuccessListener(unused ->{
                        finish();
                    });
        }else{
            StorageReference storeref = FirebaseStorage.getInstance()
                    .getReference("Event_Posters")
                    .child(eventId + ".jpg");

            storeref.putFile(posterUri).addOnSuccessListener(task ->{
                storeref.getDownloadUrl().addOnSuccessListener(downloadURl ->{
                    event.posterURL = downloadURl.toString();

                    // need to change from finish() to PreviewEvent Activity screen
                    db.collection("Events").document(eventId)
                            .set(event)
                            .addOnSuccessListener(unused ->{
                                finish();
                            });
                });
            }).addOnFailureListener(e ->{
                Toast.makeText(this, "Poster Upload Failed:" + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            });
        }
    }




    /**
     * Handles the result of the image picker activity.
     *
     * @param requestCode The request code used when starting the activity.
     * @param resultCode  The result returned by the activity.
     * @param data        The returned intent containing selected image data.
     *
     * Function behavior:
     * - If the user cancels or fails to select an image, reset the poster preview to default.
     * - If an image is successfully selected, store its URI and display it in the preview.
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

}
