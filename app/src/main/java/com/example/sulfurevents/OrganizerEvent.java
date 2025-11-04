package com.example.sulfurevents;

public class OrganizerEvent {

    public String eventId;
    public String organizerId;
    public String EventName;
    public String description;
    public String startDate;
    public String endDate;
    public String location;
    public String limitGuests;
    public String link;    // <--- add this
    public String qrCode;

    int Capacity;

    //emphyt constructor because the organizer adds

    public OrganizerEvent(){};

    public OrganizerEvent(String eventId, String organizerId,
                          String EventTitle, String description,
                          String startDate, String endDate,
                          String location, String limitGuests,
                          String link, String qrCode) {

        this.eventId = eventId;
        this.organizerId = organizerId;
        this.EventName = EventTitle;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.limitGuests = limitGuests;
        this.link = link;
        this.qrCode = qrCode;
    }

//    public void addEventCardToMainPage(String EventTitle, String location, int Capacity, String link, String startDate) {
//
//        return true;
//    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getEventTitle() {
        return EventName;
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


    //Setters

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setEventTitle(String eventTitle) {
        this.EventName = eventTitle;
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


    //    // Constructor
//    public Event(String eventName, String date, String location, int capacity) {
//        this.EventName = eventName;
//        this.Date = date;
//        this.Location = location;
//        this.Capacity = capacity;
//    }
//
//    // Getters
//    public String getEventName() {
//        return EventName;
//    }
//
//    public String getDate() {
//        return Date;
//    }
//
//    public String getLocation() {
//        return Location;
//    }
//
//    public int getCapacity() {
//        return Capacity;
//    }
//
//    // Setters
//
//    public void setEventName(String eventName) {
//        EventName = eventName;
//    }
//
//    public void setDate(String date) {
//        Date = date;
//    }
//
//    public void setLocation(String location) {
//        Location = location;
//    }
//
//    public void setCapacity(int capacity) {
//        Capacity = capacity;
//    }
}
