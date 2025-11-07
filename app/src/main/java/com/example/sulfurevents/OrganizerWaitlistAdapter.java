package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
//import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
// Java docs for part 3 done

/**
 * RecyclerView adapter for displaying entrants on an event's waiting list.
 *
 * <p>Each item row shows the entrant's name, email (if available), and device ID.
 * The adapter is fed by {@link OrganizerWaitlistActivity} once all profile data is loaded.
 *
 * <p>Author: sulfur (CMPUT 301 – Part 3)
 */
public class OrganizerWaitlistAdapter extends RecyclerView.Adapter<OrganizerWaitlistAdapter.Holder> {

    /** List of User objects containing profile details. */
    private final List<User> users;

    /** Corresponding device IDs (parallel list to users). */
    private final List<String> deviceIds;

    //private final Set<String> selectedIds = new HashSet<>();

    /**
     * Constructs a WaitlistAdapter.
     *
     * @param users     List of User models (profile info)
     * @param deviceIds Parallel list of device IDs
     */
    public OrganizerWaitlistAdapter(List<User> users, List<String> deviceIds) {
        this.users = users;
        this.deviceIds = deviceIds;
    }


    /**
     * Creates a new ViewHolder by inflating the waitlist row layout.
     *
     * @param parent   The parent ViewGroup that hosts the row.
     * @param viewType The view type (unused here).
     * @return A Holder containing the inflated row view.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_waitlist_entrant, parent, false);
        return new Holder(v);
    }


    /**
     * Binds waitlisted user data to the row UI at the given position.
     *
     * @param h         The ViewHolder for the row.
     * @param position  The index of the user in the list.
     *
     * Displays the user's name, email, and device ID.
     */
    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = users.get(position);
        String id = deviceIds.get(position);

        h.name.setText(u.getName() != null ? u.getName() : "(Unnamed)");
        String email = (u.getEmail() != null && !u.getEmail().isEmpty()) ? u.getEmail() : "(no email)";
        h.email.setText("Email: " + email);
        h.deviceId.setText("Device: " + id);
    }


    /**
     * Returns the number of users in the waitlist.
     *
     * @return Total number of waitlisted users.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for a single waiting list row.
     * Holds references to TextViews for name, email, and device ID
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, deviceId, email;

        /**
         * Initializes UI references for the waitlist row.
         *
         * @param itemView The row layout view.
         */
        Holder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}

