// AdminProfilesActivity
// This activity lets the admin browse all user profiles, view their info (email, phone),
// and delete profiles if needed.

// AdminProfilesActivity
// This activity lets the admin browse all user profiles,
// search by email, and delete profiles (placeholder behavior for now).

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AdminProfilesActivity extends AppCompatActivity {

    private Button btnBack;
    private EditText etSearchProfile;
    private ListView listViewProfiles;

    private AdminProfilesListAdapter adapter;
    private List<ProfileModel> profileList = new ArrayList<>();
    private List<ProfileModel> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profiles_activity);

        // UI elements
        btnBack = findViewById(R.id.btnBackProfiles);
        etSearchProfile = findViewById(R.id.etSearchProfile);
        listViewProfiles = findViewById(R.id.listViewProfiles);

        // back btn
        btnBack.setOnClickListener(v -> finish());

        // placeholder data
        profileList.add(new ProfileModel("alex@email.com", "123-456-7890"));
        profileList.add(new ProfileModel("bella@email.com", ""));
        profileList.add(new ProfileModel("chris@email.com", "987-654-3210"));
        profileList.add(new ProfileModel("dana@email.com", ""));

        filteredList.addAll(profileList);

        // set adapter
        adapter = new AdminProfilesListAdapter(this, filteredList);
        listViewProfiles.setAdapter(adapter);

        // search functionality
        etSearchProfile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProfiles(String query) {
        filteredList.clear();
        for (ProfileModel profile : profileList) {
            if (profile.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(profile);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
