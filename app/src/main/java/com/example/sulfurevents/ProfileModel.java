// ProfileModel
// This class defines the structure for a user profile (email + phone).
// Used by AdminProfilesActivity to display and manage users.

package com.example.sulfurevents;

import com.google.firebase.database.Exclude;

/**
 * This class defines a profile model for the SulfurEvents application.
 * A profile contains basic user information such as email, phone, device ID,
 * user type, and name. It is stored in the Firestore "Profiles" collection.
 */
public class ProfileModel {

    private String profileId;
    private String email;
    private String phone;
    private String deviceId;
    private String name;

    private boolean isAdmin;
    private boolean isOrganizer;
    private boolean isEntrant;

    private Boolean notificationsEnabled;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public ProfileModel() { }

    /**
     * Constructor for creating a new ProfileModel.
     * @param email The user's email address
     * @param phone The user's phone number
     * @param deviceId The user's device ID
     */
    public ProfileModel(String email, String phone, String deviceId) {
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;

        // default new user
        this.isEntrant = true;     // anyone signing up is an entrant
        this.isOrganizer = false;  // organizer only when they create an event
        this.isAdmin = false;      // admins assigned manually
    }

    /**
     * Gets the Firestore document ID for this profile.
     * @return The profile ID
     */

    @Exclude
    public String getProfileId() { return profileId; }

    /**
     * Sets the Firestore document ID for this profile.
     * @param profileId The profile ID
     */
    @Exclude
    public void setProfileId(String profileId) { this.profileId = profileId; }

    /**
     * Gets the user's email.
     * @return The email.
     */
    public String getEmail() { return email; }

    /**
     * Gets the user's phone number.
     * @return The phone number
     */
    public String getPhone() { return phone; }

    /**
     * Gets the user's device ID.
     * @return The device ID
     */
    public String getDeviceId() { return deviceId; }

    /**
     * Gets the user's name.
     * @return The user's name
     */
    public String getName() { return name; }

    /**
     * Sets the user's name.
     * @param name The user's name
     */
    public void setName(String name) { this.name = name; }

    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public boolean getIsOrganizer() { return isOrganizer; }
    public void setIsOrganizer(boolean isOrganizer) { this.isOrganizer = isOrganizer; }

    public boolean getIsEntrant() { return isEntrant; }
    public void setIsEntrant(boolean isEntrant) { this.isEntrant = isEntrant; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }


}
