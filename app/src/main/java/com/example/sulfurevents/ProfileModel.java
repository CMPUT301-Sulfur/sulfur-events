// ProfileModel
// This class defines the structure for a user profile (email + phone).
// Used by AdminProfilesActivity to display and manage users.

package com.example.sulfurevents;

public class ProfileModel {
    private String profileId;    // Firestore document ID
    private String email;
    private String phone;
    private String deviceId;
    private String userType;     // "entrant" or "organizer"
    private String name;

    public ProfileModel() { }

    public ProfileModel(String email, String phone, String deviceId, String userType) {
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
        this.userType = userType;
    }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDeviceId() { return deviceId; }
    public String getUserType() { return userType; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
