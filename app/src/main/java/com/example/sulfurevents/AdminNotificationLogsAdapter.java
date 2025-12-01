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

/**
 * Adapter for displaying notification log entries in the admin interface.
 * Each row shows the message, event name, type, and timestamp of a log entry.
 */
public class AdminNotificationLogsAdapter extends RecyclerView.Adapter<AdminNotificationLogsAdapter.ViewHolder> {

    private ArrayList<NotificationLogItem> logs;

    /**
     * Creates a new adapter for displaying notification logs.
     *
     * @param logs The list of NotificationLogItem objects
     */
    public AdminNotificationLogsAdapter(ArrayList<NotificationLogItem> logs) {
        this.logs = logs;
    }

    /**
     * Inflates the layout for a single notification log row.
     *
     * @param parent   The parent ViewGroup
     * @param viewType View type (unused)
     * @return A new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification_log, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data from a NotificationLogItem into the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to
     * @param position The position of the log item in the list
     */
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

    /**
     * @return Total number of log items in the list
     */
    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * ViewHolder representing a single notification log entry.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, eventName, type, timestamp;

        /**
         * Creates a ViewHolder for a single log row.
         *
         * @param itemView The inflated row layout
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.logMessage);
            eventName = itemView.findViewById(R.id.logEventName);
            type = itemView.findViewById(R.id.logType);
            timestamp = itemView.findViewById(R.id.logTimestamp);
        }
    }
}
