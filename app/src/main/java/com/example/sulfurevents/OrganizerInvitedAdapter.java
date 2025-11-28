package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.HashSet;
import java.util.List;
import java.util.Set;



// Done Java docs part 3
public class OrganizerInvitedAdapter extends RecyclerView.Adapter<OrganizerInvitedAdapter.Holder>{

    private final List<User> users;
    private final List<String> deviceIds;
    private final Set<String> selectedIds = new HashSet<>();


    /**
     * Creates an adapter for displaying invited users in a RecyclerView.
     *
     * @param users     The list of invited user profiles.
     * @param deviceIds The corresponding device IDs for each user.
     */
    public OrganizerInvitedAdapter(List<User> users, List<String> deviceIds) {
        this.users = users;
        this.deviceIds = deviceIds;
    }

    /**
     * Inflates the row layout for an invited entrant and creates a ViewHolder.
     *
     * @param parent   The parent ViewGroup that will contain the row.
     * @param viewType The view type (not used here).
     * @return A new Holder instance with the inflated row view.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_invited_entrant, parent, false);
        return new Holder(v);
    }


    /**
     * Binds invited user data to the row UI and manages selection state.
     *
     * @param h         The ViewHolder containing row UI elements.
     * @param position  The position of the user in the list.
     *
     * Sets name, email, and device ID. Handles checkbox and row click events
     * to select or unselect invited users for cancellation or replacement.
     */
    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = users.get(position);
        String id = deviceIds.get(position);

        h.name.setText(u.getName() != null ? u.getName() : "(Unnamed)");
        String email = (u.getEmail() != null && !u.getEmail().isEmpty()) ? u.getEmail() : "(no email)";
        h.email.setText("Email: " + email);
        h.deviceId.setText("Device: " + id);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(selectedIds.contains(id));


        View.OnClickListener toggle = v -> {
            if (selectedIds.contains(id)) selectedIds.remove(id);
            else selectedIds.add(id);
            notifyItemChanged(h.getAdapterPosition());
        };

        h.itemView.setOnClickListener(toggle);
        h.cb.setOnClickListener(toggle);

    }


    /**
     * Returns the total number of invited users in the list.
     *
     * @return The number of users.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns a copy of the currently selected device IDs.
     *
     * @return A set of selected device IDs.
     */
    public Set<String> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    /**
     * Clears all selected entries and refreshes the RecyclerView display.
     */
    public void clearSelection(){
        selectedIds.clear();
        notifyDataSetChanged();
    }


    /**
     * ViewHolder that stores references to the UI elements for an invited user row.
     * Used for efficient RecyclerView binding.
     */
    static class Holder extends RecyclerView.ViewHolder{
        TextView name, deviceId, email;
        CheckBox cb;

        /**
         * Initializes the UI element references from the item view.
         *
         * @param itemView The root view of the invited user row layout.
         */
        public Holder(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbSelect);
            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}