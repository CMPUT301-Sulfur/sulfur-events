package com.example.sulfurevents;

public class OrganizerEvents {

    String EventName;
    String Date;
    String Location;
    int TotalCapacity;
    int CurrentCapacity;


    // Constructor
    public OrganizerEvents(String eventName, String date, String location) {
        this.EventName = eventName;
        this.Date = date;
        this.Location = location;
        //this.TotalCapacity = totalCapacity;
        //this.CurrentCapacity = currentCapacity;
    }

    //Getters


    public String getEventName() {
        return EventName;
    }

    public String getDate() {
        return Date;
    }

    public String getLocation() {
        return Location;
    }

//    public int getTotalCapacity() {
//        return TotalCapacity;
//    }

    public int getCurrentCapacity() {
        return CurrentCapacity;
    }

    // testing





}
