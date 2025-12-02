package com.example.sulfurevents;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the lottery-style selection of entrants from an event's waiting list.
 * This class randomly selects a specified number of users from the waiting list,
 * stores them in the selected_users collection, and sends notifications to all
 * entrants (both selected and not selected).
 */
public class SampleEntrants {

    private final FirebaseFirestore db;
    private final String eventId;
    private final Integer limit;

    /**
     * Constructs a SampleEntrants instance for a specific event.
     *
     * @param eventId The ID of the event to sample entrants from
     * @param limit   The maximum number of entrants to select. If null or <= 0, all entrants are selected
     */
    public SampleEntrants(String eventId, Integer limit) {
        this.db = FirebaseFirestore.getInstance();
        this.eventId = eventId;
        this.limit = limit;
    }

    /**
     * Callback interface for handling the result of the entrant selection process.
     */
    public interface OnSelectionCompleteListener {
        /**
         * Called when entrant selection completes successfully.
         *
         * @param selectedUsers List of user IDs that were selected
         */
        void onSelectionComplete(List<String> selectedUsers);

        /**
         * Called when an error occurs during the selection process.
         *
         * @param e The exception that occurred
         */
        void onError(Exception e);
    }

    /**
     * Represents a selected user document stored in Firestore.
     * Each selected user is stored in the Events/{eventId}/selected_users collection.
     */
    public static class SelectedUser {
        private String userId;

        /** Empty constructor required for Firestore */
        public SelectedUser() {
        }

        /**
         * Constructs a SelectedUser with the specified user ID.
         *
         * @param userId The ID of the selected user
         */
        public SelectedUser(String userId) {
            this.userId = userId;
        }

        /**
         * Gets the user ID of this selected user.
         *
         * @return The user ID
         */
        public String getUserId() {
            return userId;
        }
    }

    /**
     * Performs the entrant selection process:
     * 1. Retrieves the event details to get the event name
     * 2. Fetches all users from the waiting list
     * 3. Randomly selects the specified number of winners (or all if no limit)
     * 4. Stores selected users in the selected_users collection
     * 5. Sends notifications to all entrants (invited or not selected)
     *
     * @param listener Callback to handle success or failure of the selection process
     */
    public void selectEntrants(OnSelectionCompleteListener listener) {
        // Step 1: Get the event document to retrieve the event name
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    // Extract event name from the document
                    String eventName = null;
                    if (eventDoc.exists()) {
                        eventName = eventDoc.getString("eventName");
                    }
                    // Fallback to default name if field is missing
                    if (eventName == null) {
                        eventName = "Event";
                    }

                    // Step 2: Get all users from the waiting list
                    String finalEventName = eventName;
                    db.collection("Events")
                            .document(eventId)
                            .collection("waiting_list")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<String> entrants = new ArrayList<>();

                                // Collect all user IDs from the waiting list
                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    // Document ID represents the device/user ID
                                    entrants.add(doc.getId());
                                }

                                // Check if there are any entrants to select from
                                if (entrants.isEmpty()) {
                                    listener.onError(new Exception("No entrants found in waiting list"));
                                    return;
                                }

                                // Step 3: Randomly select winners based on the limit
                                List<String> selectedUsers;
                                if (limit == null || limit <= 0) {
                                    // No limit set — select all entrants
                                    selectedUsers = new ArrayList<>(entrants);
                                    Log.d("SampleEntrants", "No limit set — selecting all entrants.");
                                } else {
                                    // Shuffle the list for random selection
                                    Collections.shuffle(entrants);
                                    int numToSelect = Math.min(limit, entrants.size());
                                    selectedUsers = entrants.subList(0, numToSelect);
                                    Log.d("SampleEntrants", "Selecting " + numToSelect + " entrants out of " + entrants.size());
                                }

                                // Step 4: Write selected users to Firestore
                                List<Task<Void>> tasks = new ArrayList<>();
                                for (String userId : selectedUsers) {
                                    // Add each selected user to the selected_users collection
                                    tasks.add(
                                            db.collection("Events")
                                                    .document(eventId)
                                                    .collection("selected_users")
                                                    .document(userId)
                                                    .set(new SelectedUser(userId))
                                                    .addOnFailureListener(e ->
                                                            Log.e("Firestore", "Failed to add selected user: " + userId, e))
                                    );
                                }

                                // Step 5: After all writes complete, create notifications for ALL entrants
                                Tasks.whenAllComplete(tasks)
                                        .addOnSuccessListener(done -> {
                                            // Notify the callback that selection is complete
                                            listener.onSelectionComplete(selectedUsers);

                                            // Create and send notifications to all entrants
                                            for (String entrantId : entrants) {
                                                boolean wasSelected = selectedUsers.contains(entrantId);

                                                // Build notification document
                                                Map<String, Object> notif = new HashMap<>();
                                                notif.put("eventId", eventId);
                                                notif.put("eventName", finalEventName);
                                                notif.put("timestamp", System.currentTimeMillis());
                                                notif.put("read", false);

                                                if (wasSelected) {
                                                    // User was selected — send invitation notification
                                                    notif.put("type", "INVITED");
                                                    notif.put("message", "You were selected for " + finalEventName + ". Open it to accept or decline.");
                                                } else {
                                                    // User was not selected — send rejection notification
                                                    notif.put("type", "NOT_SELECTED");
                                                    notif.put("message", "You were not selected for " + finalEventName + " in the first draw.");
                                                }

                                                // Add notification to user's notifications subcollection
                                                db.collection("Profiles")
                                                        .document(entrantId)
                                                        .collection("notifications")
                                                        .add(notif);
                                            }
                                        })
                                        .addOnFailureListener(listener::onError);
                            });
                });
    }
}