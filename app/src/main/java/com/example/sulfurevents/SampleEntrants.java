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

public class SampleEntrants {

    private final FirebaseFirestore db;
    private final String eventId;
    private final Integer limit;

    public SampleEntrants(String eventId, Integer limit) {
        this.db = FirebaseFirestore.getInstance();
        this.eventId = eventId;
        this.limit = limit;
    }

    public interface OnSelectionCompleteListener {
        void onSelectionComplete(List<String> selectedUsers);
        void onError(Exception e);
    }

    public static class SelectedUser {
        private String userId;

        public SelectedUser() {
        }

        public SelectedUser(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }

    public void selectEntrants(OnSelectionCompleteListener listener) {
        // 1) get the event, so we can read eventName
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    // this is your actual field name
                    String eventName = null;
                    if (eventDoc.exists()) {
                        eventName = eventDoc.getString("eventName");
                    }
                    // we’ll still fall back just in case the field is missing
                    if (eventName == null) {
                        eventName = "Event";
                    }

                    // 2) now get everyone on the waiting list
                    String finalEventName = eventName;
                    db.collection("Events")
                            .document(eventId)
                            .collection("waiting_list")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<String> entrants = new ArrayList<>();

                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    // doc id = device/user id
                                    entrants.add(doc.getId());
                                }

                                if (entrants.isEmpty()) {
                                    listener.onError(new Exception("No entrants found in waiting list"));
                                    return;
                                }

                                // 3) pick winners
                                List<String> selectedUsers;
                                if (limit == null || limit <= 0) {
                                    selectedUsers = new ArrayList<>(entrants);
                                    Log.d("SampleEntrants", "No limit set — selecting all entrants.");
                                } else {
                                    Collections.shuffle(entrants);
                                    int numToSelect = Math.min(limit, entrants.size());
                                    selectedUsers = entrants.subList(0, numToSelect);
                                    Log.d("SampleEntrants", "Selecting " + numToSelect + " entrants out of " + entrants.size());
                                }

                                // 4) write selected_users docs (what you already had)
                                List<Task<Void>> tasks = new ArrayList<>();
                                for (String userId : selectedUsers) {
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

                                // 5) after all writes, create notifications for EVERYONE
                                Tasks.whenAllComplete(tasks)
                                        .addOnSuccessListener(done -> {
                                            // tell the caller
                                            listener.onSelectionComplete(selectedUsers);

                                            for (String entrantId : entrants) {
                                                boolean wasSelected = selectedUsers.contains(entrantId);

                                                Map<String, Object> notif = new HashMap<>();
                                                notif.put("eventId", eventId);
                                                notif.put("eventName", finalEventName); // <- now guaranteed not null
                                                notif.put("timestamp", System.currentTimeMillis());
                                                notif.put("read", false);

                                                if (wasSelected) {
                                                    notif.put("type", "INVITED");
                                                    notif.put("message", "You were selected for " + finalEventName + ". Open it to accept or decline.");
                                                } else {
                                                    notif.put("type", "NOT_SELECTED");
                                                    notif.put("message", "You were not selected for " + finalEventName + " in the first draw.");
                                                }

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
