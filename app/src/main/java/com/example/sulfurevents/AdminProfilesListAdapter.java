// AdminProfilesListAdapter
// Custom adapter to display a list of user profiles in a ListView.
// Shows email and phone number, and handles delete button clicks.

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdminProfilesListAdapter extends ArrayAdapter<ProfileModel> {

    public AdminProfilesListAdapter(Context context, List<ProfileModel> profiles) {
        super(context, 0, profiles);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_profile, parent, false);
        }

        ProfileModel profile = getItem(position);

        TextView tvEmail = convertView.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = convertView.findViewById(R.id.tvProfilePhone);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteProfile);

        tvEmail.setText("Email: " + profile.getEmail());

        String phone = profile.getPhone();

        if (phone == null || phone.isEmpty()) {
            phone = "Not provided";
        }

        tvPhone.setText("Phone: " + phone);

        btnDelete.setOnClickListener(v -> Toast.makeText(getContext(), "Delete clicked for " + profile.getEmail(),
                Toast.LENGTH_SHORT).show()
        );

        return convertView;
    }
}
