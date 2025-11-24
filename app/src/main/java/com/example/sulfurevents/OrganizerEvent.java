package com.example.sulfurevents;
public class OrganizerEvent {

    public String eventId;
    public String organizerId;
    public String eventName;
    public String description;
    public String startDate;
    public String endDate;
    public String location;
    public String limitGuests;
    public String link;    // <--- add this
    public String qrCode;
    public String organizerEmail;

    public String posterURL;


    // Done java docs for Part 3

    /**
     * Default constructor required for Firestore data mapping.
     */
    //empty constructor because the organizer adds
    public OrganizerEvent() {
    }


    /**
     * Creates a fully-initialized OrganizerEvent object.
     *
     * @param eventId        Unique ID of the event.
     * @param organizerId    ID of the organizer who created the event.
     * @param EventTitle     Name/title of the event.
     * @param description    Description or details about the event.
     * @param startDate      Event start date.
     * @param endDate        Event end date.
     * @param location       Location where the event will take place.
     * @param limitGuests    Maximum number of allowed attendees.
     * @param link           Optional link associated with the event.
     * @param qrCode         Base64-encoded QR code used for event check-in.
     * @param organizerEmail Organizer's contact email.
     * @param posterURL      URL of the uploaded event poster image (nullable).
     */
    public OrganizerEvent(String eventId, String organizerId,
                          String EventTitle, String description,
                          String startDate, String endDate,
                          String location, String limitGuests,
                          String link, String qrCode, String organizerEmail, String posterURL) {

        this.eventId = eventId;
        this.organizerId = organizerId;
        this.eventName = EventTitle;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.limitGuests = limitGuests;
        this.link = link;
        this.qrCode = qrCode;
        this.organizerEmail = organizerEmail;
        this.posterURL = posterURL;
    }


    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getEventName() {
        return eventName;
    }
    public String getDescription() {
        return description;
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

    public String getLink() {
        return link;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }


    //Setters

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setLink(String link) {
        this.link = link;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setorganizerEmail() {
        this.organizerEmail = organizerEmail;
    }




}