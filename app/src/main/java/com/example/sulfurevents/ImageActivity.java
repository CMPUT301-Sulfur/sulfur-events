package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Activity for displaying a full-screen image preview.
 * Loads images from URLs using Glide library.
 */
public class ImageActivity extends AppCompatActivity {

    private ImageView fullImageView;
    private ImageView exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        // Initialize ImageViews
        fullImageView = findViewById(R.id.fullImageView);
        exitButton = findViewById(R.id.ExitButton);

        // Get the image URL from the intent
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Load the image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // Optional placeholder
                    .error(R.drawable.ic_launcher_foreground) // Optional error image
                    .into(fullImageView);
        }

        // Exit button to close the activity
        exitButton.setOnClickListener(v -> finish());

        // Optional: Click on image to close the activity
        fullImageView.setOnClickListener(v -> finish());
    }
}