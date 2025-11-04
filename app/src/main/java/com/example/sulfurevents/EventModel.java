// EventModel
// This class defines the structure of one event (name, organizer email, status).
// It helps store and pass event data cleanly inside the app.

package com.example.sulfurevents;

public class EventModel {
    private String eventId;
    private String eventName;
    private String organizerEmail;
    private String status;
    private String imageUrl;

    public EventModel() { }

    public EventModel(String eventName, String organizerEmail, String status, String imageUrl) {
        this.eventName = eventName;
        this.organizerEmail = organizerEmail;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
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
    public String getImageUrl() {
        return imageUrl;
    }
}
