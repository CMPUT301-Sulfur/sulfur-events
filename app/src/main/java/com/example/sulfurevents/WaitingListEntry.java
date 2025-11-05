package com.example.sulfurevents;

import com.google.firebase.Timestamp;

public class WaitingListEntry {
    private Timestamp joinedAt;

    public WaitingListEntry() {}

    public WaitingListEntry(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }
}
