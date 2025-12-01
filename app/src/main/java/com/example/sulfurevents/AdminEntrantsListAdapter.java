package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter used by administrators to display the list of entrants.
 * Each list item shows the entrant's name and Firestore ID, and notifies
 * a listener when an entrant is selected.
 */
public class AdminEntrantsListAdapter extends RecyclerView.Adapter<AdminEntrantsListAdapter.VH> {

    /**
     * Listener interface for handling clicks on individual entrant items.
     */
    public interface OnUserClickListener {
        /**
         * Called when an entrant row is clicked.
         *
         * @param userId   The Firestore ID of the clicked entrant
         * @param userName The name of the clicked entrant
         */
        void onUserClick(String userId, String userName);
    }

    private final List<User> users;
    private final List<String> userIds;
    private final OnUserClickListener listener;

    /**
     * Creates a new adapter for displaying entrants.
     *
     * @param users    List of user profile objects
     * @param userIds  List of Firestore document IDs matching each user
     * @param listener Callback triggered when a user is clicked
     */
    public AdminEntrantsListAdapter(List<User> users, List<String> userIds, OnUserClickListener listener) {
        this.users = users;
        this.userIds = userIds;
        this.listener = listener;
    }

    /**
     * Inflates the XML layout for each row in the RecyclerView.
     *
     * @param parent   The parent ViewGroup
     * @param viewType The type of view (unused)
     * @return A new ViewHolder instance
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_entrant, parent, false);
        return new VH(v);
    }

    /**
     * Binds a user entry to the ViewHolder at the given position.
     *
     * @param h        The ViewHolder to bind
     * @param position The index of the user in the list
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = users.get(position);
        String id = userIds.get(position);

        h.tvName.setText(u.getName());
        h.tvId.setText(id);

        h.itemView.setOnClickListener(v -> listener.onUserClick(id, u.getName()));
    }

    /**
     * @return The total number of user items in the list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder representing a single entrant row in the list.
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvId;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.itemUserName);
        }
    }
}
