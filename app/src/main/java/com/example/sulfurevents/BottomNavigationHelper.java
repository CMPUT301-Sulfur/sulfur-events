package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(BottomNavigationView bottomNavigationView, Context context) {

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_navigation) {
                // Go to ProfileActivity
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
                return true;

//            } else if (id == R.id.qr_scanner_navigation) {
//                // Go to QR scanner (if you have one)
//                Intent intent = new Intent(context, QRScannerActivity.class);
//                context.startActivity(intent);
//                return true;

            } else if (id == R.id.organizer_navigation) {
                // Go to organizer’s events
                Intent intent = new Intent(context, OrganizerActivity.class);
                context.startActivity(intent);
                return true;

            } else if (id == R.id.entrant_events_navigation) {
                // ✅ NEW: Go to EntrantActivity for joinable events
                Intent intent = new Intent(context, EntrantActivity.class);
                context.startActivity(intent);
                return true;
            }

            return false;
        });
    }
}