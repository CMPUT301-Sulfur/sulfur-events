package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class EventHistoryAdapter
        extends RecyclerView.Adapter<EventHistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onViewEvent(NotificationItem item);
    }

    private final List<NotificationItem> data;
    private final OnHistoryClickListener listener;

    public EventHistoryAdapter(List<NotificationItem> data,
                               OnHistoryClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_event_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder,
                                 int position) {
        NotificationItem item = data.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvStatus;
        private final TextView tvDate;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(NotificationItem item, OnHistoryClickListener listener) {
            String name = (item.eventName != null && !item.eventName.isEmpty())
                    ? item.eventName
                    : "(Unknown event)";
            tvEventName.setText(name);

            String status;
            if ("INVITED".equals(item.type)) {
                status = "Selected (invited)";
            } else if ("NOT_SELECTED".equals(item.type)) {
                status = "Not selected";
            } else if ("ACCEPTED".equals(item.type)) {
                status = "Accepted";
            } else if ("DECLINED".equals(item.type)) {
                status = "Declined";
            } else if ("CANCELLED".equals(item.type)) {
                status = "Cancelled";
            } else {
                status = "Update";
            }
            tvStatus.setText(status);

            if (item.timestamp > 0) {
                DateFormat df = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT);
                tvDate.setText(df.format(new Date(item.timestamp)));
            } else {
                tvDate.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewEvent(item);
                }
            });
        }
    }
}
