package com.example.sulfurevents;

public class NotificationLogItem {

    private String eventId;
    private String eventName;
    private String message;
    private String recipientId;
    private String senderId;
    private String senderRole;
    private long timestamp;
    private String type;

    public NotificationLogItem() {}

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getMessage() { return message; }
    public String getRecipientId() { return recipientId; }
    public String getSenderId() { return senderId; }
    public String getSenderRole() { return senderRole; }
    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }
}
