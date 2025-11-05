// EventModel
// This class defines the structure of one event (name, organizer email, status).
// It helps store and pass event data cleanly inside the app.

package com.example.sulfurevents;

/**
 * This class defines an event model.
 * An event has an ID, name, organizer email, status, and an image URL.
 */
public class EventModel {
    private String eventId;
    private String eventName;
    private String organizerEmail;
    private String status;
    private String imageUrl;
    private String description;

    /** Empty constructor required for Firestore */
    public EventModel() { }

    /**
     * Gets the event ID
     * @return The event ID
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the event ID
     * @param eventId The event ID
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the event name
     * @return The event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the event name
     * @param eventName The event name
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the organizer email
     * @return The organizer email
     */
    public String getOrganizerEmail() {
        return organizerEmail;
    }

<<<<<<< HEAD
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
=======
    /**
     * Sets the organizer email
     * @param organizerEmail The organizer email
     */
    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    /**
     * Gets the event status
     * @return The event status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the event status
     * @param status The event status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the event image URL
     * @return The image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the event image URL
     * @param imageUrl The image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
>>>>>>> origin/main
}
