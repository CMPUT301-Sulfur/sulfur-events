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

public class OrganizerInvitedAdapter extends RecyclerView.Adapter<OrganizerInvitedAdapter.Holder>{

    private final List<User> users;
    private final List<String> deviceIds;
    private final Set<String> selectedIds = new HashSet<>();

    public OrganizerInvitedAdapter(List<User> users, List<String> deviceIds) {
        this.users = users;
        this.deviceIds = deviceIds;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_invited_entrant, parent, false);
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

    @Override
    public int getItemCount() {
        return users.size();
    }

    public Set<String> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }
    public void clearSelection(){
        selectedIds.clear();
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder{
        TextView name, deviceId, email;
        CheckBox cb;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbSelect);
            name = itemView.findViewById(R.id.tvName);
            deviceId = itemView.findViewById(R.id.tvDeviceId);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
