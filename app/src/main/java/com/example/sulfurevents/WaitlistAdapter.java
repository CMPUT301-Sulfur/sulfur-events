package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying entrants on an event's waiting list.
 *
 * <p>Each item row shows the entrant's name, email (if available), and device ID.
 * The adapter is fed by {@link OrganizerWaitlistActivity} once all profile data is loaded.
 *
 * <p>Author: Daniel Minchenko (CMPUT 301 – Part 3)
 */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.Holder> {

    /** List of User objects containing profile details. */
    private final List<User> users;

    /** Corresponding device IDs (parallel list to users). */
    private final List<String> deviceIds;

    /**
     * Constructs a WaitlistAdapter.
     *
     * @param users     List of User models (profile info)
     * @param deviceIds Parallel list of device IDs
     */
    public WaitlistAdapter(List<User> users, List<String> deviceIds) {
        this.users = users;
        this.deviceIds = deviceIds;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_waitlist_entrant, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = users.get(position);
        String id = deviceIds.get(position);

        // Display basic info; fallbacks if missing
        h.name.setText(u.getName() != null ? u.getName() : "(Unnamed)");
        h.deviceId.setText("Device: " + id);

        String email = (u.getEmail() != null && !u.getEmail().isEmpty())
                ? u.getEmail() : "(no email)";
        h.email.setText("Email: " + email);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for a single waiting list row.
     * Holds references to TextViews for name, email, and device ID.
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, deviceId, email;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}

