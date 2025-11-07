package com.example.sulfurevents;

public class NotificationItem {
    public String docId;
    public String eventId;
    public String eventName;
    public String type;
    public String message;
    public long timestamp;
    public boolean read;

    public static NotificationItem fromDoc(com.google.firebase.firestore.DocumentSnapshot doc) {
        NotificationItem item = new NotificationItem();
        item.docId = doc.getId();
        item.eventId = doc.getString("eventId");
        item.eventName = doc.getString("eventName");
        item.type = doc.getString("type");
        item.message = doc.getString("message");
        Long ts = doc.getLong("timestamp");
        item.timestamp = ts != null ? ts : 0L;
        Boolean r = doc.getBoolean("read");
        item.read = r != null && r;
        return item;
    }
}
