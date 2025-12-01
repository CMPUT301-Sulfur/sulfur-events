package com.example.sulfurevents;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * The {@code OrganizerCreateEventActivity} class allows organizers to create new events
 * in the SulfurEvents application.
 */
public class OrganizerCreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEvent";
    private static final int IMAGE_REQUEST = 1;

    private Uri posterUri = null;
    private FirebaseFirestore db;
    private String DeviceID;
    private User CurrentUser;
    private SwitchCompat switchGeolocation;
    private TextView tvGeolocationStatus;
    private Geocoder geocoder;
    private String organizerProfileEmail;

    /**
     * Shows a custom styled toast message matching the app's black and gold theme
     */
    private void showStyledToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();

        if (toastView != null) {
            // Set background to dark with gold border
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#111111")); // Dark background
            background.setStroke(3, Color.parseColor("#D4AF37")); // Gold border
            background.setCornerRadius(12);
            toastView.setBackground(background);

            // Style the text
            TextView toastText = toastView.findViewById(android.R.id.message);
            if (toastText != null) {
                toastText.setTextColor(Color.parseColor("#D4AF37")); // Gold text
                toastText.setTextSize(16);
                toastText.setPadding(40, 20, 40, 20);
            }
        }

        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_event_activity);

        Intent intent = getIntent();
        boolean isEdit = intent.getBooleanExtra("isEdit", false);
        if (isEdit) {
            enableEditMode(intent);
        }

        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.GenerateEventButton), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        DeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        geocoder = new Geocoder(this, Locale.getDefault());

        switchGeolocation = findViewById(R.id.switchGeolocation);
        tvGeolocationStatus = findViewById(R.id.tvGeolocationStatus);

        updateSwitchColors(false);

        switchGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(isChecked);
        });

        db.collection("Profiles").document(DeviceID).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            organizerProfileEmail = u.getEmail();
                        }
                    }
                });

        ImageButton backButton = findViewById(R.id.Back_Button);
        backButton.setOnClickListener(v -> finish());

        Button GenerateQRCodeEventButon = findViewById(R.id.GenerateEventButton);
        GenerateQRCodeEventButon.setOnClickListener(view ->{
            CreateEvent();
        });

        FrameLayout poster = findViewById(R.id.posterUploadArea);
        poster.setOnClickListener(view ->{
            Intent pickintent = new Intent(Intent.ACTION_PICK);
            pickintent.setType("image/*");
            startActivityForResult(pickintent,IMAGE_REQUEST);
        });

        EditText start = findViewById(R.id.etStartDate);
        EditText end = findViewById(R.id.etEndDate);

        setdate(start);
        setdate(end);
    }

    private void updateSwitchColors(boolean isChecked) {
        if (isChecked) {
            switchGeolocation.setThumbTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#4CAF50")));
            switchGeolocation.setTrackTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#A5D6A7")));

            tvGeolocationStatus.setText("ON");
            tvGeolocationStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        } else {
            switchGeolocation.setThumbTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#F44336")));
            switchGeolocation.setTrackTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#EF9A9A")));

            tvGeolocationStatus.setText("OFF");
            tvGeolocationStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }
    }

    private Bitmap generateQR(String value) throws Exception{
        com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
        return encoder.encodeBitmap(value, com.google.zxing.BarcodeFormat.QR_CODE, 500, 500);
    }

    private String bitmaptobase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private boolean validateAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);

            if (addresses == null || addresses.isEmpty()) {
                EditText etLocation = findViewById(R.id.etLocation);
                etLocation.setError("Please enter a valid address");
                showStyledToast("Address not found. Please enter a valid address.");
                return false;
            }

            Address validAddress = addresses.get(0);
            String formattedAddress = validAddress.getAddressLine(0);

            Log.d(TAG, "Valid address: " + formattedAddress);

            EditText etLocation = findViewById(R.id.etLocation);
            etLocation.setText(formattedAddress);

            return true;

        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
            showStyledToast("Unable to verify address. Please check your internet connection.");
            return false;
        }
    }

    private void CreateEvent() {
        String title = ((EditText)findViewById(R.id.etEventName)).getText().toString();
        String description = ((EditText)findViewById(R.id.etDescription)).getText().toString();
        String start = ((EditText)findViewById(R.id.etStartDate)).getText().toString();
        String end = ((EditText)findViewById(R.id.etEndDate)).getText().toString();
        String location = ((EditText)findViewById(R.id.etLocation)).getText().toString();
        String limit = ((EditText)findViewById(R.id.etLimitGuests)).getText().toString();
        String waitingLimit = ((EditText)findViewById(R.id.etWaitingListLimit)).getText().toString();
        String OGEmail = organizerProfileEmail;

        boolean geolocationEnabled = switchGeolocation.isChecked();

        if (!location.isBlank() && !validateAddress(location)) {
            showStyledToast("Please enter a valid address.");
            return;
        }

        DocumentReference newEventRef = db.collection("Events").document();
        String eventId = newEventRef.getId();

        if (getIntent().getBooleanExtra("isEdit", false)) {
            String EditID = getIntent().getStringExtra("eventId");
            EditEvent(EditID, title, description, start, end, location, limit, waitingLimit);
            return;
        }

        Bitmap qrBitmap;
        String qrBase64;

        try {
            String deepLink = "sulfurevents://event/" + eventId;
            qrBitmap = generateQR(deepLink);
            qrBase64 = bitmaptobase64(qrBitmap);
        } catch (Exception e) {
            showStyledToast("Cannot create event: QR code generation failed");
            return;
        }

        if(title.isBlank() || description.isBlank() || start.isBlank() ||
                end.isBlank() || location.isBlank() || limit.isBlank() || OGEmail.isBlank()) {
            showStyledToast("Please fill all fields");
            return;
        }

        if(!isDateValid(start, end)) {
            showStyledToast("End date cannot be before start date");
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
        event.geolocationEnabled = geolocationEnabled;

        if (waitingLimit != null && !waitingLimit.isBlank()) {
            event.waitingListLimit = waitingLimit;
        } else {
            event.waitingListLimit = "";
        }

        if (posterUri == null) {
            event.posterURL = null;

            newEventRef.set(event)
                    .addOnSuccessListener(unused -> {
                        String message = geolocationEnabled
                                ? "Event created with geolocation enabled!"
                                : "Event created successfully!";
                        showStyledToast(message);

                        saveQRToGallery(qrBitmap, eventId);

                        db.collection("Profiles").document(DeviceID)
                                .update("isOrganizer", true);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showStyledToast("Failed to create event: " + e.getMessage());
                    });

        } else {
            StorageReference storeref = FirebaseStorage.getInstance()
                    .getReference("Event_Posters")
                    .child(eventId + ".jpg");

            storeref.putFile(posterUri).addOnSuccessListener(task -> {
                storeref.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    event.posterURL = downloadUrl.toString();

                    newEventRef.set(event)
                            .addOnSuccessListener(unused -> {
                                String message = geolocationEnabled
                                        ? "Event created with geolocation enabled!"
                                        : "Event created successfully!";
                                showStyledToast(message);

                                saveQRToGallery(qrBitmap, eventId);

                                db.collection("Profiles").document(DeviceID)
                                        .update("isOrganizer", true);

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showStyledToast("Failed to create event: " + e.getMessage());
                            });
                });
            }).addOnFailureListener(e -> {
                showStyledToast("Poster upload failed: " + e.getMessage());
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

            showStyledToast("QR code saved to gallery!");

        } catch (Exception e) {
            e.printStackTrace();
            showStyledToast("Failed to save QR code");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        ImageView eventposter = findViewById(R.id.eventPosterPreview);

        if(requestCode == IMAGE_REQUEST && resultCode != RESULT_OK){
            posterUri = null;
            eventposter.setImageResource(R.drawable.upload);
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
                                selectedMonth + 1,
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

        Integer Syear = Integer.parseInt(StartSplit[2]);
        Integer Eyear = Integer.parseInt(EndSplit[2]);
        Integer Sday = Integer.parseInt(StartSplit[1]);
        Integer Eday = Integer.parseInt(EndSplit[1]);
        Integer Smonth = Integer.parseInt(StartSplit[0]);
        Integer Emonth = Integer.parseInt(EndSplit[0]);

        if(Eyear > Syear) return true;
        if(Eyear < Syear) return false;

        if(Emonth > Smonth) return true;
        if(Emonth < Smonth) return false;

        return Eday >= Sday;
    }

    private void enableEditMode(Intent intent){
        ((EditText)findViewById(R.id.etEventName))
                .setText(intent.getStringExtra("eventName"));

        ((EditText)findViewById(R.id.etDescription))
                .setText(intent.getStringExtra("description"));

        ((EditText)findViewById(R.id.etStartDate))
                .setText(intent.getStringExtra("startDate"));

        ((EditText)findViewById(R.id.etEndDate))
                .setText(intent.getStringExtra("endDate"));

        ((EditText)findViewById(R.id.etLocation))
                .setText(intent.getStringExtra("location"));

        ((EditText)findViewById(R.id.etLimitGuests))
                .setText(intent.getStringExtra("capacity"));

        String waitingLimitExtra = intent.getStringExtra("waitingListLimit");
        if (waitingLimitExtra != null && !waitingLimitExtra.isEmpty()) {
            ((EditText)findViewById(R.id.etWaitingListLimit))
                    .setText(waitingLimitExtra);
        }

        Button btn = findViewById(R.id.GenerateEventButton);
        btn.setText("Save Changes");

        String posterURL = intent.getStringExtra("posterURL");
        ImageView preview = findViewById(R.id.eventPosterPreview);

        if (posterURL != null && !posterURL.isEmpty()) {
            Glide.with(this).load(posterURL).into(preview);
            posterUri = Uri.parse(posterURL);
        }
    }

    private void EditEvent(String eventId, String title, String description,
                           String start, String end, String location,
                           String limit, String waitingLimit){

        if(title.isBlank() || description.isBlank() || start.isBlank() ||
                end.isBlank() || location.isBlank() || limit.isBlank()){
            showStyledToast("Please fill all fields");
            return;
        }

        if(!isDateValid(start, end)){
            showStyledToast("End date cannot be before start date");
            return;
        }

        if (posterUri != null && !posterUri.toString().startsWith("http")) {
            StorageReference storeref = FirebaseStorage.getInstance()
                    .getReference("Event_Posters")
                    .child(eventId + ".jpg");

            storeref.putFile(posterUri).addOnSuccessListener(task -> {
                storeref.getDownloadUrl().addOnSuccessListener(downloadURL -> {
                    db.collection("Events").document(eventId)
                            .update(
                                    "eventName", title,
                                    "description", description,
                                    "startDate", start,
                                    "endDate", end,
                                    "location", location,
                                    "limitGuests", limit,
                                    "organizerEmail", organizerProfileEmail,
                                    "posterURL", downloadURL.toString(),
                                    "waitingListLimit", waitingLimit != null ? waitingLimit : ""
                            )
                            .addOnSuccessListener(unused -> {
                                showStyledToast("Event updated successfully");
                                finish();
                            });
                });
            }).addOnFailureListener(e -> {
                showStyledToast("Failed to upload poster: " + e.getMessage());
            });
        } else {
            db.collection("Events").document(eventId)
                    .update(
                            "eventName", title,
                            "description", description,
                            "startDate", start,
                            "endDate", end,
                            "location", location,
                            "limitGuests", limit,
                            "organizerEmail", organizerProfileEmail,
                            "waitingListLimit", waitingLimit != null ? waitingLimit : ""
                    )
                    .addOnSuccessListener(unused -> {
                        showStyledToast("Event updated successfully");
                        finish();
                    });
        }
    }
}