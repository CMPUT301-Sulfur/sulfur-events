// EventModel
// This class defines the structure of one event (name, organizer email, status).
// It helps store and pass event data cleanly inside the app.

package com.example.sulfurevents;

/**
 * Represents an event stored in Firestore.
 * Each event has basic details like name, description, location,
 * date range, capacity, organizer info, and optional poster image.
 */
public class EventModel {

    private String eventId;
    private String eventName;
    private String description;
    private String organizerEmail;
    private String organizerId;
    private String startDate;
    private String endDate;
    private String location;
    private String limitGuests; // capacity
    private String status;      // "open", "closed", etc.
    private String posterURL;

    /** Empty constructor required for Firestore */
    public EventModel() { }

    // -------------------- Getters --------------------

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getLocation() {
        return location;
    }

    public String getLimitGuests() {
        return limitGuests;
    }

    public String getStatus() {
        return status;
    }

    public String getPosterURL() {
        return posterURL;
    }

    // -------------------- Setters --------------------

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLimitGuests(String limitGuests) {
        this.limitGuests = limitGuests;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }
}

