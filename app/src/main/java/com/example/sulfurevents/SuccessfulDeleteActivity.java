package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SuccessfulDeleteActivity
 * This activity displays a success message after a user account has been deleted.
 * It provides a button to return to the welcome screen to create a new account if desired.
 */
public class SuccessfulDeleteActivity extends AppCompatActivity {

    /** Button to return to the welcome screen */
    private Button returnWelcomeButton;

    /**
     * Called when the activity is first created.
     * Initializes the UI and sets up the return button to navigate back to WelcomeEntrantActivity.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deleted_user);

        returnWelcomeButton = findViewById(R.id.return_welcome_button);

        returnWelcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToWelcome();
            }
        });
    }

    /**
     * Navigate to the welcome screen
     */
    private void navigateToWelcome() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Intent intent = new Intent(SuccessfulDeleteActivity.this, WelcomeEntrantActivity.class);
        intent.putExtra("deviceId", deviceId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}