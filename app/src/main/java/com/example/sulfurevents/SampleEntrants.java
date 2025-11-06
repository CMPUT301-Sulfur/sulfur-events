package com.example.sulfurevents;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void selectEntrants(OnSelectionCompleteListener listener) {
        db.collection("Events")
                .document(eventId)
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> entrants = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        entrants.add(doc.getId()); // assuming doc ID = user ID
                    }

                    if (entrants.isEmpty()) {
                        listener.onError(new Exception("No entrants found in waiting list"));
                        return;
                    }

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

                    for (String userId : selectedUsers) {
                        db.collection("Events")
                                .document(eventId)
                                .collection("selected_users")
                                .document(userId)
                                .set(new SelectedUser(userId))
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "Failed to add selected user: " + userId, e));
                    }

                    listener.onSelectionComplete(selectedUsers);
                })
                .addOnFailureListener(listener::onError);
    }

    public static class SelectedUser {
        private String userId;

        public SelectedUser() {}

        public SelectedUser(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }
}

