package com.example.sulfurevents;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying event history items in a RecyclerView.
 * Shows event name, status, timestamp, and message for each notification.
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.HistoryViewHolder> {

    private final List<NotificationItem> items;
    private final OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onViewEvent(NotificationItem item);
    }

    public EventHistoryAdapter(List<NotificationItem> items, OnHistoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_event_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        NotificationItem item = items.get(position);

        holder.tvEventName.setText(item.eventName != null ? item.eventName : "Unknown Event");
        holder.tvStatus.setText(getStatusText(item.type));
        holder.tvTimestamp.setText(getFormattedDate(item.timestamp));
        holder.tvMessage.setText(item.message != null ? item.message : "");

        // Color code based on type
        int statusColor = getStatusColor(item.type);
        holder.tvStatus.setTextColor(statusColor);

        // Set read/unread indicator
        if (!item.read) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E8F4F8"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        // Click listener to view event details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewEvent(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Returns a user-friendly status text based on notification type
     */
    private String getStatusText(String type) {
        if (type == null) {
            return "Unknown";
        }

        switch (type.toUpperCase()) {
            case "INVITED":
                return "Selected";
            case "NOT_SELECTED":
                return "Not Selected";
            case "WAITING":
                return "Waiting List";
            case "ENROLLED":
                return "Enrolled";
            case "CANCELLED":
                return "Cancelled";
            default:
                return type;
        }
    }

    /**
     * Returns a formatted date string from timestamp
     */
    private String getFormattedDate(long timestamp) {
        if (timestamp == 0) {
            return "Unknown date";
        }

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
        return sdf.format(date);
    }

    /**
     * Returns a color based on the notification type
     */
    private int getStatusColor(String type) {
        if (type == null) {
            return Color.GRAY;
        }

        switch (type.toUpperCase()) {
            case "INVITED":
            case "ENROLLED":
                return Color.parseColor("#4CAF50"); // Green
            case "NOT_SELECTED":
            case "CANCELLED":
                return Color.parseColor("#F44336"); // Red
            case "WAITING":
                return Color.parseColor("#FF9800"); // Orange
            default:
                return Color.GRAY;
        }
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvStatus;
        TextView tvTimestamp;
        TextView tvMessage;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}