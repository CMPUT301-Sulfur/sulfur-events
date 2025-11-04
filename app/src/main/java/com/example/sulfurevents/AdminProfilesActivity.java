// AdminProfilesActivity
// This activity lets the admin browse all user profiles, view their info (email, phone),
// and delete profiles if needed.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import javax.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AdminProfilesActivity extends AppCompatActivity {

    private ListView listViewProfiles;
    private ArrayList<ProfileModel> profileList;
    private AdminProfilesListAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference profilesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profiles_activity);

        Button btnBack = findViewById(R.id.btnBackProfiles);
        btnBack.setOnClickListener(v -> finish());

        listViewProfiles = findViewById(R.id.listViewProfiles);
        profileList = new ArrayList<>();
        adapter = new AdminProfilesListAdapter(this, profileList);
        listViewProfiles.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        profilesRef = db.collection("Profiles");

        loadProfilesFromFirestore();
    }

    private void loadProfilesFromFirestore() {
        profilesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(AdminProfilesActivity.this, "Failed to load profiles: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Listen failed.", e);
                    return;
                }

                profileList.clear();
                for (DocumentSnapshot doc : snapshots) {
                    ProfileModel profile = doc.toObject(ProfileModel.class);
                    if (profile != null) {
                        profile.setProfileId(doc.getId());
                        profileList.add(profile);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void deleteProfile(String profileId) {
        profilesRef.document(profileId).delete().addOnSuccessListener(aVoid ->
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show());
    }
}
