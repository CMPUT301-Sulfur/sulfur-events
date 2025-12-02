package com.example.sulfurevents;

/**
 * Represents a notification log entry stored in Firestore.
 * Each notification log item contains information about a notification sent to a user,
 * including the event details, sender information, message content, and timestamp.
 * This is used for tracking notification history and displaying notification logs.
 */
public class NotificationLogItem {

    private String eventId;
    private String eventName;
    private String message;
    private String recipientId;
    private String senderId;
    private String senderRole;
    private long timestamp;
    private String type;

    /** Empty constructor required for Firestore */
    public NotificationLogItem() {}

    // -------------------- Getters --------------------

    /**
     * Gets the ID of the event associated with this notification.
     *
     * @return The event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the name of the event associated with this notification.
     *
     * @return The event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the notification message content.
     *
     * @return The message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the ID of the user who received this notification.
     *
     * @return The recipient's user ID
     */
    public String getRecipientId() {
        return recipientId;
    }

    /**
     * Gets the ID of the user who sent this notification.
     *
     * @return The sender's user ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Gets the role of the user who sent this notification.
     * Typically "organizer" or "admin".
     *
     * @return The sender's role
     */
    public String getSenderRole() {
        return senderRole;
    }

    /**
     * Gets the timestamp when this notification was sent.
     *
     * @return The timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the type/category of this notification.
     * Examples: "invite", "cancellation", "update", "reminder"
     *
     * @return The notification type
     */
    public String getType() {
        return type;
    }
}