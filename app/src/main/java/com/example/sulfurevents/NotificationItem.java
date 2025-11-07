package com.example.sulfurevents;

/**
 * Data model representing a single notification addressed to an entrant.
 * <p>
 * Each instance of {@code NotificationItem} corresponds to a document stored in Firestore
 * under {@code Profiles/<deviceId>/notifications/<notificationId>}. This object holds
 * metadata and user-facing text for notifications such as:
 * <ul>
 *     <li>“You were selected for [Event Name]” (INVITED)</li>
 *     <li>“You were not selected for [Event Name]” (NOT_SELECTED)</li>
 * </ul>
 * <p>
 * This class directly supports user stories:
 * <ul>
 *     <li><b>US 01.04.01</b> – Notification when chosen from the waiting list</li>
 *     <li><b>US 01.04.02</b> – Notification when not chosen for the event</li>
 *     <li><b>US 01.05.02</b> – Invitation acceptance workflow</li>
 *     <li><b>US 01.05.03</b> – Invitation decline workflow</li>
 * </ul>
 * It provides a static factory method {@link #fromDoc(com.google.firebase.firestore.DocumentSnapshot)}
 * for converting Firestore documents into model instances.
 */
public class NotificationItem {

    /**
     * Firestore document ID of this notification (used for marking as read or updating).
     */
    public String docId;

    /**
     * Identifier of the event this notification is associated with.
     */
    public String eventId;

    /**
     * Human-readable name of the related event.
     */
    public String eventName;

    /**
     * Type of the notification.
     * Common values include:
     * <ul>
     *     <li>{@code INVITED} – entrant was selected from the waiting list.</li>
     *     <li>{@code NOT_SELECTED} – entrant was not selected for participation.</li>
     * </ul>
     */
    public String type;

    /**
     * Textual message shown to the entrant (e.g., “You were selected for ...”).
     */
    public String message;

    /**
     * UNIX timestamp (in milliseconds) indicating when the notification was created.
     */
    public long timestamp;

    /**
     * Indicates whether the entrant has already viewed or responded to this notification.
     */
    public boolean read;

    /**
     * Creates a {@code NotificationItem} object from a Firestore document.
     * This method safely extracts all expected fields and applies default
     * values if a field is missing or null.
     *
     * @param doc Firestore document representing a notification
     * @return a fully populated {@code NotificationItem} instance
     */
    public static NotificationItem fromDoc(com.google.firebase.firestore.DocumentSnapshot doc) {
        NotificationItem item = new NotificationItem();
        item.docId = doc.getId();
        item.eventId = doc.getString("eventId");
        item.eventName = doc.getString("eventName");
        item.type = doc.getString("type");
        item.message = doc.getString("message");
        Long ts = doc.getLong("timestamp");
        item.timestamp = ts != null ? ts : 0L;
        Boolean r = doc.getBoolean("read");
        item.read = r != null && r;
        return item;
    }
}
