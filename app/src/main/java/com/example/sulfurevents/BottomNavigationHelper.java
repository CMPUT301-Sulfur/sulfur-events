package com.example.sulfurevents;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(BottomNavigationView bottomNavigationView, Activity currentActivity) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_navigation) {
                if (!(currentActivity instanceof MainActivity)) {
                    Intent intent = new Intent(currentActivity, MainActivity.class);
                    currentActivity.startActivity(intent);
                }
                return true;

//            } else if (id == R.id.qr_scanner_navigation) {
//                if (!(currentActivity instanceof QrScannerActivity)) {
//                    Intent intent = new Intent(currentActivity, QrScannerActivity.class);
//                    currentActivity.startActivity(intent);
//                }
//                return true;

            } else if (id == R.id.organizer_navigation) {
                if (!(currentActivity instanceof OrganizerActivity)) {
                    Intent intent = new Intent(currentActivity, OrganizerActivity.class);
                    currentActivity.startActivity(intent);
                }
                return true;
            }

            return false;
        });
    }
}
