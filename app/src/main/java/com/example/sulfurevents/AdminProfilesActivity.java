// AdminProfilesActivity
// This activity lets the admin browse all user profiles, view their info (email, phone),
// and delete profiles if needed.

package com.example.sulfurevents;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
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

        // Back button
        ImageButton back = findViewById(R.id.btnBackProfiles);
        back.setOnClickListener(v -> finish());

        // List setup
        listViewProfiles = findViewById(R.id.listViewProfiles);
        profileList = new ArrayList<>();
        adapter = new AdminProfilesListAdapter(this, profileList);
        listViewProfiles.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
        profilesRef = db.collection("Profiles");

        loadProfilesFromFirestore();
    }

    /**
     * Loads all profiles from Firestore and updates the ListView.
     */
    private void loadProfilesFromFirestore() {
        profilesRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(AdminProfilesActivity.this,
                        "Failed to load profiles: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            profileList.clear();

            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots) {
                    ProfileModel profile = doc.toObject(ProfileModel.class);
                    if (profile != null) {
                        profile.setProfileId(doc.getId()); // set doc ID
                        profileList.add(profile);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Deletes a profile document from Firestore by deviceId.
     */
    public void deleteProfile(String profileId) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete this profile?\n\nThis action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {

                    // Same as before (you asked to keep this)
                    android.app.ProgressDialog progressDialog =
                            new android.app.ProgressDialog(this);
                    progressDialog.setMessage("Deleting profile...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    db.collection("Profiles")
                            .document(profileId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();

                                // Popup instead of Toast
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Profile Deleted")
                                        .setMessage("The profile has been successfully deleted.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();

                                // Popup instead of Toast
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Delete Failed")
                                        .setMessage("Error: " + e.getMessage())
                                        .setPositiveButton("OK", null)
                                        .show();
                            });

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
