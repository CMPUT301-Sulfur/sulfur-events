// EventModel
// This class defines the structure of one event (name, organizer email, status).
// It helps store and pass event data cleanly inside the app.

package com.example.sulfurevents;

public class EventModel {
    private String eventName;
    private String organizerEmail;
    private String status;

    public EventModel(String eventName, String organizerEmail, String status) {
        this.eventName = eventName;
        this.organizerEmail = organizerEmail;
        this.status = status;
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
}
