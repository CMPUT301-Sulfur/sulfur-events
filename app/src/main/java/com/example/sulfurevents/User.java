package com.example.sulfurevents;

/**
 * User
 * This class represents a user profile in the application.
 * It stores essential user information including device ID, personal details,
 * and admin status. Used throughout the app for user authentication and profile management.
 * This model is compatible with Firestore for seamless data storage and retrieval.
 */
public class User {
    /** Unique device identifier used as the primary key for the user */
    private String deviceId;

    /** The user's full name */
    private String name;

    /** The user's email address */
    private String email;

    /** The user's phone number (optional) */
    private String phone;

    /** Flag indicating whether the user has admin privileges */
    private boolean isAdmin;

    private boolean notificationsEnabled = true;

    /**
     * Parameterized constructor to create a User with all fields.
     * @param deviceId The unique device identifier
     * @param name The user's full name
     * @param email The user's email address
     * @param phone The user's phone number
     * @param isAdmin True if the user has admin privileges, false otherwise
     */
    public User(String deviceId, String name, String email, String phone, boolean isAdmin) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.notificationsEnabled = true; // default ON
    }

    /**
     * No-argument constructor required for Firestore deserialization.
     * Initializes isAdmin to false by default.
     */
    public User() {
        this.isAdmin = false;
    }

    /**
     * Gets the device ID.
     * @return The unique device identifier
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID.
     * @param deviceId The unique device identifier
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the user's name.
     * @return The user's full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     * @param name The user's full name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     * @return The user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email The user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's phone number.
     * @return The user's phone number
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
     * Checks if the user has admin privileges.
     * @return True if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Sets the user's admin status.
     * @param admin True to grant admin privileges, false to revoke
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}