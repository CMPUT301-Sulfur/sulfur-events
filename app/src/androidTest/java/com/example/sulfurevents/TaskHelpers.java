package com.example.sulfurevents;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

/**
 * Utility class containing Firestore helper methods used
 * exclusively by instrumented tests for notification logic.
 * <p>
 * These methods mimic the same behavior implemented in
 * {@link NotificationsActivity}, {@link EventDetailsActivity}, and
 * {@link SampleEntrants} to simplify testing without launching full activities.
 */
public class TaskHelpers {

    /**
     * Simulates inviting the first entrant from the waiting list to an event.
     * <p>
     * - Moves the first user from {@code waiting_list} → {@code invited_list}.<br>
     * - Creates a Firestore notification document under
     * {@code Profiles/<userId>/notifications} with {@code type="INVITED"}.
     *
     * @param db      Firestore instance
     * @param eventId ID of the event document
     */
    public static void inviteFirstWaitingUser(FirebaseFirestore db, String eventId) throws Exception {
        DocumentSnapshot doc = Tasks.await(db.collection("Events").document(eventId).get());
        if (!doc.exists()) return;

        List<String> waiting = (List<String>) doc.get("waiting_list");
        String eventName = doc.getString("eventName");
        if (eventName == null) eventName = "Event";

        if (waiting == null || waiting.isEmpty()) return;
        String next = waiting.get(0);

        Tasks.await(
                db.collection("Events").document(eventId)
                        .update(
                                "waiting_list", removeFrom(waiting, next),
                                "invited_list", addTo((List<String>) doc.get("invited_list"), next)
                        )
        );

        Map<String, Object> notif = new HashMap<>();
        notif.put("eventId", eventId);
        notif.put("eventName", eventName);
        notif.put("type", "INVITED");
        notif.put("message", "You were selected for " + eventName + ". Tap to accept or decline.");
        notif.put("timestamp", System.currentTimeMillis());
        notif.put("read", false);

        Tasks.await(
                db.collection("Profiles").document(next)
                        .collection("notifications").add(notif)
        );
    }

    /**
     * Checks if an event has reached full capacity and, if so,
     * sends a "NOT_SELECTED" notification to all entrants still
     * on the waiting list.
     *
     * @param db      Firestore instance
     * @param eventId ID of the event document
     */
    public static void checkAndNotifyNotSelectedIfFull(FirebaseFirestore db, String eventId) throws Exception {
        DocumentSnapshot eventDoc = Tasks.await(db.collection("Events").document(eventId).get());
        if (!eventDoc.exists()) return;

        String eventName = eventDoc.getString("eventName");
        if (eventName == null) eventName = "Event";

        List<String> waiting  = (List<String>) eventDoc.get("waiting_list");
        List<String> enrolled = (List<String>) eventDoc.get("enrolled_list");
        List<String> invited  = (List<String>) eventDoc.get("invited_list");

        if (waiting == null) waiting = new ArrayList<>();
        if (enrolled == null) enrolled = new ArrayList<>();
        if (invited == null) invited = new ArrayList<>();

        // your schema uses limitGuests (String)
        String capStr = eventDoc.getString("limitGuests");
        int capacity = 0;
        try { capacity = Integer.parseInt(capStr); } catch (Exception ignored) {}

        int taken = enrolled.size() + invited.size();
        boolean isFull = capacity > 0 && taken >= capacity;
        if (!isFull) return;

        for (String deviceId : waiting) {
            if (enrolled.contains(deviceId) || invited.contains(deviceId)) continue;

            Map<String, Object> notif = new HashMap<>();
            notif.put("eventId", eventId);
            notif.put("eventName", eventName);
            notif.put("type", "NOT_SELECTED");
            notif.put("message", "You were not selected for " + eventName + ".");
            notif.put("timestamp", System.currentTimeMillis());
            notif.put("read", false);

            Tasks.await(
                    db.collection("Profiles").document(deviceId)
                            .collection("notifications").add(notif)
            );
        }

        Tasks.await(db.collection("Events").document(eventId)
                .update("waiting_list", new ArrayList<String>()));
    }

    /**
     * Simulates drawing a replacement user when an invited entrant declines.
     * <p>
     * - Moves the first waiting user → {@code invited_list}.<br>
     * - Sends that user an "INVITED" notification.
     *
     * @param db      Firestore instance
     * @param eventId ID of the event document
     */
    public static void drawReplacementAndNotify(FirebaseFirestore db, String eventId) throws Exception {
        DocumentSnapshot doc = Tasks.await(db.collection("Events").document(eventId).get());
        if (!doc.exists()) return;

        List<String> waiting = (List<String>) doc.get("waiting_list");
        String eventName = doc.getString("eventName");
        if (eventName == null) eventName = "Event";

        if (waiting == null || waiting.isEmpty()) return;
        String nextId = waiting.get(0);

        Tasks.await(
                db.collection("Events").document(eventId)
                        .update(
                                "waiting_list", removeFrom(waiting, nextId),
                                "invited_list", addTo((List<String>) doc.get("invited_list"), nextId)
                        )
        );

        Map<String, Object> notif = new HashMap<>();
        notif.put("eventId", eventId);
        notif.put("eventName", eventName);
        notif.put("type", "INVITED");
        notif.put("message", "You were selected for " + eventName + ". Tap to accept or decline.");
        notif.put("timestamp", System.currentTimeMillis());
        notif.put("read", false);

        Tasks.await(
                db.collection("Profiles").document(nextId)
                        .collection("notifications").add(notif)
        );
    }

    /** Removes a specific ID from a list. */
    private static List<String> removeFrom(List<String> list, String id) {
        List<String> copy = new ArrayList<>(list);
        copy.remove(id);
        return copy;
    }

    /** Adds an ID to a list, creating it if null. */
    private static List<String> addTo(List<String> list, String id) {
        if (list == null) list = new ArrayList<>();
        list.add(id);
        return list;
    }
}
