package com.example.sulfurevents;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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

import kotlin.LateinitKt;

public class CreateEventActivity extends AppCompatActivity {


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
        });



    }


    // Generating QR code

    private Bitmap generateQR(String value) throws Exception{
        com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
        return encoder.encodeBitmap(value, com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
    }







}
