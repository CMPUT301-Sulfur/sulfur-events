package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Activity for displaying a full-screen image.
 * This activity receives an image URL via Intent extras and displays it
 * using Glide image loading library. The image can be dismissed by tapping on it.
 */
public class ImageActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Sets up the full-screen image view and loads the image from the provided URL.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_activity);

        // Get reference to the full-screen ImageView
        ImageView fullImage = findViewById(R.id.fullImageView);

        // Retrieve the image URL passed via Intent extras
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Load the image from URL using Glide library
        Glide.with(this)
                .load(imageUrl)
                .into(fullImage);

        // Set click listener to close the activity when image is tapped
        fullImage.setOnClickListener(v -> finish());
    }
}
