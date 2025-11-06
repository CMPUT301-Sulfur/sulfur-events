package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * RecyclerView adapter for displaying entrants on an event's waiting list.
 *
 * <p>Each item row shows the entrant's name, email (if available), and device ID.
 * The adapter is fed by {@link OrganizerWaitlistActivity} once all profile data is loaded.
 *
 * <p>Author: sulfur (CMPUT 301 – Part 3)
 */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.Holder> {

    /** List of User objects containing profile details. */
    private final List<User> users;

    /** Corresponding device IDs (parallel list to users). */
    private final List<String> deviceIds;

    private final Set<String> selectedIds = new HashSet<>();

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

        h.name.setText(u.getName() != null ? u.getName() : "(Unnamed)");
        String email = (u.getEmail() != null && !u.getEmail().isEmpty()) ? u.getEmail() : "(no email)";
        h.email.setText("Email: " + email);
        h.deviceId.setText("Device: " + id);

        // reflect selection
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(selectedIds.contains(id));

        // toggle by row or checkbox click
        View.OnClickListener toggle = v -> {
            if (selectedIds.contains(id)) selectedIds.remove(id);
            else selectedIds.add(id);
            notifyItemChanged(h.getAdapterPosition());
        };
        h.itemView.setOnClickListener(toggle);
        h.cb.setOnClickListener(toggle);
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public Set<String> getSelectedIds() { return new HashSet<>(selectedIds); }

    public void clearSelection() { selectedIds.clear(); notifyDataSetChanged(); }

    /**
     * ViewHolder class for a single waiting list row.
     * Holds references to TextViews for name, email, and device ID
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, deviceId, email;
        CheckBox cb;
        Holder(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbSelect);
            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}

