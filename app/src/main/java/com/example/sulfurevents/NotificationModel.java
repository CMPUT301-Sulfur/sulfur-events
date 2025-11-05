package com.example.sulfurevents;

public class NotificationModel {
    private String id;
    private String toProfileId;
    private String eventId;
    private String type;    // "chosen" | "not_chosen"
    private String message;
    private boolean handled;

    public NotificationModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getToProfileId() {
        return toProfileId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
