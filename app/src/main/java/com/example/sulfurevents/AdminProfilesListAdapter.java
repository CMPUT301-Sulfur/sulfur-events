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

public class AdminProfilesListAdapter extends ArrayAdapter<ProfileModel> {

    public AdminProfilesListAdapter(Context context, ArrayList<ProfileModel> list) {
        super(context, 0, list);
    }

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
        Button btnDelete = convertView.findViewById(R.id.btnDeleteProfile);

        if (profile != null) {
            tvEmail.setText("Email: " + (profile.getEmail() == null || profile.getEmail().isEmpty() ? "—" : profile.getEmail()));
            tvPhone.setText("Phone: " + (profile.getPhone() == null || profile.getPhone().isEmpty() ? "—" : profile.getPhone()));
            tvDevice.setText("Device ID: " + (profile.getDeviceId() == null || profile.getDeviceId().isEmpty() ? "—" : profile.getDeviceId()));

            btnDelete.setOnClickListener(v -> {
                if (getContext() instanceof AdminProfilesActivity) {
                    ((AdminProfilesActivity) getContext()).deleteProfile(profile.getProfileId());
                }
            });
        }

        return convertView;
    }
}
