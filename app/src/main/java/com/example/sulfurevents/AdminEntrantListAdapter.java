package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * RecyclerView adapter used to display a list of entrant profiles
 * for administrators. Each item shows a user's name and email,
 * and clicking an item triggers a callback to open their logs.
 */
public class AdminEntrantListAdapter extends RecyclerView.Adapter<AdminEntrantListAdapter.ViewHolder> {

    /**
     * Listener interface for handling clicks on a user item.
     */
    public interface OnUserClickListener {
        /**
         * Called when a user item is clicked.
         * @param user The profile that was selected
         */
        void onUserClick(ProfileModel user);
    }

    private ArrayList<ProfileModel> users;
    private OnUserClickListener listener;

    /**
     * Creates a new adapter for entrant profiles.
     *
     * @param users List of users to display
     * @param listener Callback for item selection
     */
    public AdminEntrantListAdapter(ArrayList<ProfileModel> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    /**
     * Inflates a single list item layout for each entrant.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_entrant, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds entrant information (name and email) to the list item.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileModel user = users.get(position);

        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    /**
     * Returns the number of users in the list.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class that stores references to UI elements
     * for displaying a single entrant in the list.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;

        /**
         * Creates a ViewHolder and binds UI references.
         *
         * @param itemView The inflated layout for the list item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemUserName);
            email = itemView.findViewById(R.id.itemUserEmail);
        }
    }
}
