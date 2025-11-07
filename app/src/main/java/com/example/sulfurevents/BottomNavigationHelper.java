package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class for managing bottom navigation functionality across activities.
 * <p>
 * This class provides a centralized method to configure the bottom navigation bar,
 * ensuring consistent behavior and navigation logic throughout the application.
 * </p>
 */
public class BottomNavigationHelper {

    /**
     * Sets up the bottom navigation view with navigation handlers and selected item highlighting.
     * <p>
     * This method configures the bottom navigation bar to:
     * <ul>
     *     <li>Highlight the appropriate menu item based on the current activity</li>
     *     <li>Handle navigation between different activities when menu items are selected</li>
     *     <li>Prevent redundant activity launches when already on the target screen</li>
     * </ul>
     * </p>
     *
     * @param bottomNavigationView The BottomNavigationView instance to configure
     * @param context The context of the calling activity, used to determine the current
     *                screen and to launch new activities. Should be an instance of one
     *                of the supported activities
     *
     */
    public static void setupBottomNavigation(BottomNavigationView bottomNavigationView, Context context) {

        // Clear any existing listeners to prevent duplicates
        bottomNavigationView.setOnItemSelectedListener(null);

        // Set the selected item based on the current activity context
        if (context instanceof ProfileActivity) {
            bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        } else if (context instanceof OrganizerActivity) {
            bottomNavigationView.setSelectedItemId(R.id.organizer_navigation);
        } else if (context instanceof EntrantActivity) {
            bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);
        } else if (context instanceof NotificationsActivity) {
            bottomNavigationView.setSelectedItemId(R.id.notifications_navigation);
        }

        // Set up the item selection listener for navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Navigate to Profile/Home screen
            if (id == R.id.home_navigation) {
                if (!(context instanceof ProfileActivity)) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

                // Navigate to Organizer screen
            } else if (id == R.id.organizer_navigation) {
                if (!(context instanceof OrganizerActivity)) {
                    Intent intent = new Intent(context, OrganizerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

                // Navigate to Entrant Events screen
            } else if (id == R.id.entrant_events_navigation) {
                if (!(context instanceof EntrantActivity)) {
                    Intent intent = new Intent(context, EntrantActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
                return true;

                // Navigate to Notifications screen
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
