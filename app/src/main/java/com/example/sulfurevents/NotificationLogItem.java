package com.example.sulfurevents;

public class NotificationLogItem {
    public String senderId;
    public String senderRole;
    public String recipientId;
    public String eventId;
    public String eventName;
    public String type;
    public String message;
    public long timestamp;

    public NotificationLogItem() {} // Firestore needs empty constructor
}
