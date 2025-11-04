package com.example.sulfurevents;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;


public class OrganizerCreateEventActivity extends AppCompatActivity {


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



    }


    // Generating QR code

    private Bitmap generateQR(String value) throws Exception{
        com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
        return encoder.encodeBitmap(value, com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
    }

    private String bitmaptobase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

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

        try{
            // assign returned bitmap
            qrBitmap = generateQR(eventId);
            // convert to base 64
            qrBase64 = bitmaptobase64(qrBitmap);

        }catch (Exception e){
            // we should add a toast and say "cannot create event"

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

        // need to change from finish() to PreviewEvent Activity screen
        db.collection("Events").document(eventId)
                .set(event)
                .addOnSuccessListener(unused ->{
                    finish();
                });
    }

}
