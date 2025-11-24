package com.example.sulfurevents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * This helper class centralizes setup for the bottom navigation bar.
 * It follows a utility design pattern to reduce repeated navigation logic
 * across multiple activities in the SulfurEvents app.
 */
public class BottomNavigationHelper {
    /**
     * Sets up navigation actions for the bottom navigation bar.
     * Handles switching between main sections of the app such as
     * Home, Organizer, Entrant, and Notifications.
     *
     * @param bottomNavigationView The BottomNavigationView to configure
     * @param context The current activity context
     */

    public static void setupBottomNavigation(BottomNavigationView bottomNavigationView, Context context) {

        bottomNavigationView.setOnItemSelectedListener(null);

        if (context instanceof ProfileActivity) {
            bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        } else if (context instanceof OrganizerActivity) {
            bottomNavigationView.setSelectedItemId(R.id.organizer_navigation);
        } else if (context instanceof EntrantActivity) {
            bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);
        }
//        else if (context instanceof NotificationsActivity) {
//            bottomNavigationView.setSelectedItemId(R.id.notifications_navigation);
//        }

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

            }
//            else if (id == R.id.notifications_navigation) {
//                if (!(context instanceof NotificationsActivity)) {
//                    Intent intent = new Intent(context, NotificationsActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    context.startActivity(intent);
//                }
//                return true;
//            }

            return false;
        });
    }
    public static void setupNotificationFab(Activity activity, int fabId, int bottomNavId) {
        FloatingActionButton fab = activity.findViewById(fabId);
        BottomNavigationView bottomNav = activity.findViewById(bottomNavId);

        if (fab == null) return;

        // If bottom nav is not on this screen, hide FAB
        if (bottomNav == null || bottomNav.getVisibility() != View.VISIBLE) {
            fab.setVisibility(View.GONE);
            return;
        }

        // Otherwise show and wire it normally
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(activity, NotificationsActivity.class);
            activity.startActivity(intent);
        });
    }

}
