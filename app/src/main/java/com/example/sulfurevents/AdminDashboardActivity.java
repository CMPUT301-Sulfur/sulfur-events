// AdminDashboardActivity
// This is the main entry screen for administrators.
// It has buttons to manage events, profiles, and images.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * This class defines the admin dashboard screen.
 * It lets administrators manage events, profiles, and images.
 */
public class AdminDashboardActivity extends AppCompatActivity {
    private Button btnManageEvents;
    private Button btnManageProfiles;
    private Button btnManageImages;

    private Button btnViewLogs;

    /**
     * Called when the activity is created.
     * Sets up the buttons and their click listeners.
     * @param savedInstanceState The saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard_activity);

        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnManageProfiles = findViewById(R.id.btnManageProfiles);
        btnManageImages = findViewById(R.id.btnManageImages);

        // go to Manage Events
        btnManageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminEventsActivity.class);
                startActivity(intent);
            }
        });

        // go to Manage Profiles
        btnManageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class);
                startActivity(intent);
            }
        });

        // go to Manage Images
        btnManageImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminImagesActivity.class);
                startActivity(intent);
            }
        });

    }
}
