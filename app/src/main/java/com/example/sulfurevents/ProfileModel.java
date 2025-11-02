// ProfileModel
// This class defines the structure for a user profile (email + phone).
// Used by AdminProfilesActivity to display and manage users.

package com.example.sulfurevents;

public class ProfileModel {
    private String email;
    private String phone;

    public ProfileModel(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
}
