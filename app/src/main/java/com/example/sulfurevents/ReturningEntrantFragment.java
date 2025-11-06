package com.example.sulfurevents;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class ReturningEntrantFragment extends Fragment {

    private TextView nameDisplay, emailDisplay, phoneDisplay;
    private Button editButton;
    private FirebaseFirestore db;
    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.returning_entrant, container, false);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        nameDisplay = view.findViewById(R.id.name_display);
        emailDisplay = view.findViewById(R.id.email_display);
        phoneDisplay = view.findViewById(R.id.phone_display);
        editButton = view.findViewById(R.id.edit_button);

        db.collection("Profiles").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            nameDisplay.setText(u.getName());
                            emailDisplay.setText(u.getEmail());
                            phoneDisplay.setText(u.getPhone());
                        }
                    }
                });

        return view;
    }
}
