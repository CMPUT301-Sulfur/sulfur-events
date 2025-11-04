package com.example.sulfurevents;

public class OrganizerEvents{

    String EventName, Date, Location;
    int Capacity;


    // Constructor
    public OrganizerEvents(String eventName, String date, String location, int capacity) {
        this.EventName = eventName;
        this.Date = date;
        this.Location = location;
        this.Capacity = capacity;
    }

    // Getters
    public String getEventName() {
        return EventName;
    }

    public String getDate() {
        return Date;
    }

    public String getLocation() {
        return Location;
    }

    public int getCapacity() {
        return Capacity;
    }

    // Setters

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setCapacity(int capacity) {
        Capacity = capacity;
    }
}
