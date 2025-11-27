package com.example.sulfurevents;

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

public class AdminNotificationLogsAdapter extends RecyclerView.Adapter<AdminNotificationLogsAdapter.VH> {

    private final List<NotificationLogItem> logs;
    private final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public AdminNotificationLogsAdapter(List<NotificationLogItem> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationLogItem item = logs.get(position);

        h.tvEvent.setText(item.eventName != null ? item.eventName : "Event");
        h.tvType.setText(item.type);
        h.tvMessage.setText(item.message);

        String time = fmt.format(new Date(item.timestamp));
        h.tvMeta.setText("From: " + item.senderRole +
                "  →  To entrant: " + item.recipientId +
                "\n" + time);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEvent, tvType, tvMessage, tvMeta;
        VH(@NonNull View v) {
            super(v);
            tvEvent = v.findViewById(R.id.tvLogEvent);
            tvType = v.findViewById(R.id.tvLogType);
            tvMessage = v.findViewById(R.id.tvLogMessage);
            tvMeta = v.findViewById(R.id.tvLogMeta);
        }
    }
}