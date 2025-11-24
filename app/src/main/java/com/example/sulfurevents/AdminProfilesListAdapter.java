// AdminImagesListAdapter
// Adapter for displaying events with images in a ListView.
// Shows thumbnail, event info, and a button to manage images.

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This class defines a custom list adapter for showing user profiles.
 * It is used in the admin profiles screen.
 */
public class AdminProfilesListAdapter extends ArrayAdapter<ProfileModel> {

    /**
     * Constructor for creating a new AdminProfilesListAdapter
     * @param context The current context
     * @param list The list of profiles to display
     */
    public AdminProfilesListAdapter(Context context, ArrayList<ProfileModel> list) {
        super(context, 0, list);
    }

    /**
     * Gets the view for a single profile item in the list
     * @param position The position of the profile in the list
     * @param convertView The recycled view to reuse
     * @param parent The parent view group
     * @return The completed list item view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_profile, parent, false);
        }

        ProfileModel profile = getItem(position);

        TextView tvEmail = convertView.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = convertView.findViewById(R.id.tvProfilePhone);
        TextView tvDevice = convertView.findViewById(R.id.tvProfileDeviceId);
        TextView tvName = convertView.findViewById(R.id.tvProfileName); // FIXED
        TextView tvEntrant = convertView.findViewById(R.id.tvProfileIsEntrant);
        TextView tvOrganizer = convertView.findViewById(R.id.tvProfileIsOrganizer);
        TextView tvAdmin = convertView.findViewById(R.id.tvProfileIsAdmin);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteProfile);

        if (profile != null) {
            tvEmail.setText("Email: " + (profile.getEmail() == null ? "—" : profile.getEmail()));
            tvPhone.setText("Phone: " + (profile.getPhone() == null ? "—" : profile.getPhone()));
            tvDevice.setText("Device ID: " + (profile.getDeviceId() == null ? "—" : profile.getDeviceId()));
            tvName.setText("Name: " + (profile.getName() == null ? "—" : profile.getName()));
            tvEntrant.setText("Entrant: " + (profile.getIsEntrant() ? "Yes" : "No"));
            tvOrganizer.setText("Organizer: " + (profile.getIsOrganizer() ? "Yes" : "No"));
            tvAdmin.setText("Admin: " + (profile.getIsAdmin() ? "Yes" : "No"));

            btnDelete.setOnClickListener(v -> {
                if (getContext() instanceof AdminProfilesActivity) {
                    ((AdminProfilesActivity) getContext()).deleteProfile(profile.getProfileId());
                }
            });
        }

        return convertView;
    }

}
