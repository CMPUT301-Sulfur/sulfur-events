package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminEntrantsListAdapter extends RecyclerView.Adapter<AdminEntrantsListAdapter.VH> {

    public interface OnUserClickListener {
        void onUserClick(String userId, String userName);
    }

    private final List<User> users;
    private final List<String> userIds;
    private final OnUserClickListener listener;

    public AdminEntrantsListAdapter(List<User> users, List<String> userIds, OnUserClickListener listener) {
        this.users = users;
        this.userIds = userIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_entrant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = users.get(position);
        String id = userIds.get(position);

        h.tvName.setText(u.getName());
        h.tvId.setText(id);

        h.itemView.setOnClickListener(v -> listener.onUserClick(id, u.getName()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvId;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.itemUserName);
//            tvId = v.findViewById(R.id.tvUserId);
        }
    }
}
