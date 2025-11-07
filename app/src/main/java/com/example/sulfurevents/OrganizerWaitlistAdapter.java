package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
//import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * {@code OrganizerWaitlistAdapter}
 * <p>
 * RecyclerView adapter for displaying entrants currently on an event’s waiting list.
 * </p>
 *
 * <h3>Purpose</h3>
 * <ul>
 *   <li>Displays basic entrant information such as name, email, and device ID.</li>
 *   <li>Used by {@link OrganizerWaitlistActivity} once profile data has been retrieved from Firestore.</li>
 *   <li>Can be easily extended to include selection behavior for future functionality.</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Currently read-only — no selection or modification behavior is active.</li>
 *   <li>Parallel lists ({@code users} and {@code deviceIds}) must stay in sync by index.</li>
 * </ul>
 *
 * <p>Author: sulfur — CMPUT 301 (Part 3)</p>
 */
public class OrganizerWaitlistAdapter extends RecyclerView.Adapter<OrganizerWaitlistAdapter.Holder> {

    /** List of {@link User} objects containing profile details (name, email, etc.). */
    private final List<User> users;

    /** Corresponding device IDs, parallel to the {@link #users} list by position. */
    private final List<String> deviceIds;

    // Optional: could later support multi-select behavior
    // private final Set<String> selectedIds = new HashSet<>();

    /**
     * Constructs a new adapter instance for a waiting list.
     *
     * @param users     list of user profile models (retrieved from Firestore)
     * @param deviceIds list of associated device IDs (same size and order as users)
     */
    public OrganizerWaitlistAdapter(List<User> users, List<String> deviceIds) {
        this.users = users;
        this.deviceIds = deviceIds;
    }

    /**
     * Inflates a row layout for each waiting list entrant.
     *
     * @param parent   the parent {@link ViewGroup}
     * @param viewType the type of the new view (unused here)
     * @return a new {@link Holder} instance for the row
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_waitlist_entrant, parent, false);
        return new Holder(v);
    }

    /**
     * Binds user and device data to a single row in the RecyclerView.
     *
     * @param h         the {@link Holder} representing this row
     * @param position  the position of the item within the list
     */
    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = users.get(position);
        String id = deviceIds.get(position);

        // Display name (fallback to placeholder if missing)
        h.name.setText(u.getName() != null ? u.getName() : "(Unnamed)");

        // Show email or fallback label
        String email = (u.getEmail() != null && !u.getEmail().isEmpty())
                ? u.getEmail() : "(no email)";
        h.email.setText("Email: " + email);

        // Show device ID label
        h.deviceId.setText("Device: " + id);

        // --- Selection behavior (currently disabled) ---
        // Code below can be reactivated for multi-select support in the future.
        //
        // h.cb.setOnCheckedChangeListener(null);
        // h.cb.setChecked(selectedIds.contains(id));
        //
        // View.OnClickListener toggle = v -> {
        //     if (selectedIds.contains(id)) selectedIds.remove(id);
        //     else selectedIds.add(id);
        //     notifyItemChanged(h.getAdapterPosition());
        // };
        // h.itemView.setOnClickListener(toggle);
        // h.cb.setOnClickListener(toggle);
    }

    /**
     * Returns the total number of waiting list entrants.
     *
     * @return number of users in the adapter
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    // --- Potential selection helpers (currently unused) ---
    //
    // public Set<String> getSelectedIds() { return new HashSet<>(selectedIds); }
    // public void clearSelection() { selectedIds.clear(); notifyDataSetChanged(); }

    /**
     * {@code Holder}
     * <p>
     * Represents a single row view within the waiting list RecyclerView.
     * Holds references to the name, email, and device ID fields for efficient reuse.
     * </p>
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, deviceId, email;
        // CheckBox cb;

        /**
         * Binds the row view elements.
         *
         * @param itemView the inflated row layout view
         */
        Holder(@NonNull View itemView) {
            super(itemView);
            // cb = itemView.findViewById(R.id.cbSelect);
            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
