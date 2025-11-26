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

/**
 * This class defines the admin profiles screen.
 * It lets administrators view and delete user profiles from Firestore.
 */
public class AdminProfilesActivity extends AppCompatActivity {

    private ListView listViewProfiles;
    private ArrayList<ProfileModel> profileList;
    private AdminProfilesListAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference profilesRef;

    /**
     * Called when the activity is created.
     * Sets up the list and loads profiles from Firestore.
     * @param savedInstanceState The saved instance state bundle
     */
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

    /**
     * Loads all profiles from the Firestore "Profiles" collection.
     * Updates the list automatically when data changes.
     */
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

    /**
     * Deletes a profile from Firestore.
     * @param profileId The ID of the profile to delete
     */
    public void deleteProfile(String profileId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Delete all events created by this profile
        db.collection("Events")
                .whereEqualTo("organizerId", profileId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }

                    // 2. Now delete the profile itself
                    profilesRef.document(profileId)
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this,
                                            "Profile and all related events deleted",
                                            Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Failed to delete profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to delete profile's events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}