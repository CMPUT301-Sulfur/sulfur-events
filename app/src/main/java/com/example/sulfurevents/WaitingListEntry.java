package com.example.sulfurevents;

public class WaitingListEntry {
    private String eventId;
    private String entrantId;
    private long timestamp;

    public WaitingListEntry() {} // Needed for Firebase

    public WaitingListEntry(String eventId, String entrantId) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.timestamp = System.currentTimeMillis(); // Auto-generate timestamp
    }

    public WaitingListEntry(String eventId, String entrantId, long timestamp) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.timestamp = timestamp;
    }

    public String getEventId()
    {
        return eventId;
    }
    public String getEntrantId()
    {
        return entrantId;
    }
    public long getTimestamp()
    {
        return timestamp;
    }
}
