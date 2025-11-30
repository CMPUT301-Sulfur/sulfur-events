package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminNotificationLogsAdapter extends RecyclerView.Adapter<AdminNotificationLogsAdapter.ViewHolder> {

    private ArrayList<NotificationLogItem> logs;

    public AdminNotificationLogsAdapter(ArrayList<NotificationLogItem> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationLogItem item = logs.get(position);

        holder.message.setText(item.getMessage());
        holder.eventName.setText("Event: " + item.getEventName());
        holder.type.setText("Type: " + item.getType());

        String formatted = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                .format(new Date(item.getTimestamp()));

        holder.timestamp.setText(formatted);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, eventName, type, timestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.logMessage);
            eventName = itemView.findViewById(R.id.logEventName);
            type = itemView.findViewById(R.id.logType);
            timestamp = itemView.findViewById(R.id.logTimestamp);
        }
    }
}
