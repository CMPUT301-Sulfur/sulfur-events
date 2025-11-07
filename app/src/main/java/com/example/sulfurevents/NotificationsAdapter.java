package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sulfurevents.NotificationItem;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifVH> {

    public interface NotificationActionListener {
        void onAccept(NotificationItem item);
        void onDecline(NotificationItem item);
        void onViewEvent(NotificationItem item);
    }

    private final List<NotificationItem> data;
    private final NotificationActionListener listener;

    public NotificationsAdapter(List<NotificationItem> data, NotificationActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotifVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifVH h, int position) {
        NotificationItem item = data.get(position);

        h.tvEventName.setText(item.eventName != null && !item.eventName.isEmpty()
                ? item.eventName
                : "Event update");
        h.tvMessage.setText(item.message != null ? item.message : "");

        // timestamp
        if (item.timestamp > 0) {
            String ts = DateFormat.getDateTimeInstance().format(new Date(item.timestamp));
            h.tvTimestamp.setText(ts);
        } else {
            h.tvTimestamp.setText("");
        }

        // default: hide action
        h.btnAccept.setVisibility(View.GONE);
        h.btnDecline.setVisibility(View.GONE);

//        h.btnView.setOnClickListener(v -> {
//            if (listener != null) listener.onViewEvent(item);
//        });

        if ("INVITED".equalsIgnoreCase(item.type)) {
            h.btnAccept.setVisibility(View.VISIBLE);
            h.btnDecline.setVisibility(View.VISIBLE);

            h.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(item);
            });
            h.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(item);
            });
        } else {
            // NOT_SELECTED or other → just view
            h.btnAccept.setVisibility(View.GONE);
            h.btnDecline.setVisibility(View.GONE);
        }

        // optional style if read
        if (item.read) {
            h.itemView.setAlpha(0.6f);
        } else {
            h.itemView.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class NotifVH extends RecyclerView.ViewHolder {
        TextView tvEventName, tvMessage, tvTimestamp;
        Button btnAccept, btnDecline;

        public NotifVH(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
//            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
