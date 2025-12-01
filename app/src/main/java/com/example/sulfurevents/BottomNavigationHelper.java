package com.example.sulfurevents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
        // Apply color scheme to bottom navigation
        applyColorScheme(bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home_navigation) {
                if (!(context instanceof ProfileActivity)) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.organizer_navigation) {
                if (!(context instanceof OrganizerActivity)) {
                    Intent intent = new Intent(context, OrganizerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.entrant_events_navigation) {
                if (!(context instanceof EntrantActivity)) {
                    Intent intent = new Intent(context, EntrantActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
                return true;

            } else if (id == R.id.entrant_history_navigation) {
                // Handle History navigation (US 01.02.03)
                if (!(context instanceof EntrantHistoryActivity)) {
                    Intent intent = new Intent(context, EntrantHistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
                return true;

            }

            return false;
        });
    }

    /**
     * Applies the black and gold color scheme to the bottom navigation view
     *
     * @param bottomNavigationView The BottomNavigationView to style
     */
    private static void applyColorScheme(BottomNavigationView bottomNavigationView) {
        // Create color state list for icons and text
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },  // selected
                new int[] { android.R.attr.state_selected }, // selected alternative
                new int[] { -android.R.attr.state_checked }  // unselected
        };

        int[] colors = new int[] {
                0xFFD4AF37,  // Gold for selected
                0xFFD4AF37,  // Gold for selected alternative
                0xFF777777   // Gray for unselected
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);

        // Apply colors to both icons and text
        bottomNavigationView.setItemIconTintList(colorStateList);
        bottomNavigationView.setItemTextColor(colorStateList);

        // Set background to black
        bottomNavigationView.setBackgroundColor(0xFF000000);

        // Remove the white background ripple effect on selected items
        bottomNavigationView.setItemRippleColor(null);

        // Create transparent color state list for item backgrounds
        int[][] bgStates = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { }
        };

        int[] bgColors = new int[] {
                0x00000000,  // Transparent for selected
                0x00000000   // Transparent for unselected
        };

        ColorStateList bgColorStateList = new ColorStateList(bgStates, bgColors);
        bottomNavigationView.setItemActiveIndicatorColor(bgColorStateList);
    }

    /**
     * Updates the highlighted item in the BottomNavigationView based on
     * the current Activity type. Each activity corresponds to one nav item.
     *
     * @param bottomNavigationView The BottomNavigationView whose selection should be updated
     * @param context              The current screen's context (used to identify the activity type)
     */
    public static void updateNavHighlighting(BottomNavigationView bottomNavigationView, Context context) {
        if (context instanceof ProfileActivity) {
            bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        } else if (context instanceof OrganizerActivity) {
            bottomNavigationView.setSelectedItemId(R.id.organizer_navigation);
        } else if (context instanceof EntrantActivity) {
            bottomNavigationView.setSelectedItemId(R.id.entrant_events_navigation);
        } else if (context instanceof EntrantHistoryActivity) {
            bottomNavigationView.setSelectedItemId(R.id.entrant_history_navigation);
        }
    }

    /**
     * Sets up the notification FloatingActionButton (FAB).
     * The FAB is only shown when the BottomNavigationView is present and visible;
     * otherwise, it is hidden. When tapped, it opens the NotificationsActivity.
     *
     * @param activity  The activity where the FAB resides
     * @param fabId     The resource ID of the FloatingActionButton
     * @param bottomNavId The resource ID of the BottomNavigationView on that screen
     */
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