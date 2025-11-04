// ImageEventModel
// Represents one event that has an image (name, organizer email, status, and image reference).

package com.example.sulfurevents;

public class ImageEventModel {
    private String eventId;
    private String eventName;
    private String organizerEmail;
    private String status;
    private String imageUrl; // from Firestore

    public ImageEventModel() {}

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }
    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
