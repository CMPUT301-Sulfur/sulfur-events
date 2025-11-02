// ImageEventModel
// Represents one event that has an image (name, organizer email, status, and image reference).

package com.example.sulfurevents;

public class ImageEventModel {
    private String eventName;
    private String organizerEmail;
    private String status;
    private int imageResId; // using a drawable placeholder for now

    public ImageEventModel(String eventName, String organizerEmail, String status, int imageResId) {
        this.eventName = eventName;
        this.organizerEmail = organizerEmail;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getEventName() {
        return eventName;
    }
    public String getOrganizerEmail() {
        return organizerEmail;
    }
    public String getStatus() {
        return status;
    }
    public int getImageResId() {
        return imageResId;
    }
}
