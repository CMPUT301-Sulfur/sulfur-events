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

    // Use Boolean (nullable) to handle cases where the field doesn't exist in Firestore yet
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
        this.notificationsEnabled = true;  // notifications enabled by default
    }

    /**
     * Gets the Firestore document ID for this profile.
     * @return The profile ID
     */
    @Exclude
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the Firestore document ID for this profile.
     * @param profileId The profile ID
     */
    @Exclude
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Gets the user's email.
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     * @param email The user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's phone number.
     * @return The phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     * @param phone The user's phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the user's device ID.
     * @return The device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the user's name.
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     * @param name The user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the user is an admin.
     * @return True if admin, false otherwise
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Sets whether the user is an admin.
     * @param isAdmin True to make admin, false otherwise
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Checks if the user is an organizer.
     * @return True if organizer, false otherwise
     */
    public boolean getIsOrganizer() {
        return isOrganizer;
    }

    /**
     * Sets whether the user is an organizer.
     * @param isOrganizer True to make organizer, false otherwise
     */
    public void setIsOrganizer(boolean isOrganizer) {
        this.isOrganizer = isOrganizer;
    }

    /**
     * Checks if the user is an entrant.
     * @return True if entrant, false otherwise
     */
    public boolean getIsEntrant() {
        return isEntrant;
    }

    /**
     * Sets whether the user is an entrant.
     * @param isEntrant True to make entrant, false otherwise
     */
    public void setIsEntrant(boolean isEntrant) {
        this.isEntrant = isEntrant;
    }

    /**
     * Gets the notification preference for this user.
     * Returns Boolean (nullable) to handle cases where field doesn't exist in Firestore.
     * @return True if notifications enabled, false if disabled, null if not set
     */
    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets the notification preference for this user.
     * @param notificationsEnabled True to enable, false to disable
     */
    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}