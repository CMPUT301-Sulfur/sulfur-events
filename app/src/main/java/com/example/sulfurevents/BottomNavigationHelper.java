package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(BottomNavigationView bottomNavigationView, Context context) {

        bottomNavigationView.setOnItemSelectedListener(null);

        if (context instanceof ProfileActivity) {
            bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        } else if (context instanceof OrganizerActivity) {
            bottomNavigationView.setSelectedItemId(R.id.organizer_navigation);
        } else if (context instanceof EntrantActivity) {
            bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);
        } else if (context instanceof NotificationsActivity) {
            bottomNavigationView.setSelectedItemId(R.id.notifications_navigation);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_navigation) {
                if (!(context instanceof ProfileActivity)) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.organizer_navigation) {
                if (!(context instanceof OrganizerActivity)) {
                    Intent intent = new Intent(context, OrganizerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.entrant_events_navigation) {
                if (!(context instanceof EntrantActivity)) {
                    Intent intent = new Intent(context, EntrantActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.notifications_navigation) {
                if (!(context instanceof NotificationsActivity)) {
                    Intent intent = new Intent(context, NotificationsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;
            }

            return false;
        });
    }
}
