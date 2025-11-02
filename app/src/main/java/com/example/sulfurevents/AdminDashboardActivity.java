// AdminDashboardActivity
// This is the main entry screen for administrators.
// It has buttons to manage events, profiles, and images.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageEvents;
    private Button btnManageProfiles;
    private Button btnManageImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard_activity);

        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnManageProfiles = findViewById(R.id.btnManageProfiles);
        btnManageImages = findViewById(R.id.btnManageImages);

        // Navigate to Manage Events
        btnManageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminEventsActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Manage Profiles
        btnManageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Manage Images
        btnManageImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminImagesActivity.class);
                startActivity(intent);
            }
        });
    }
}
