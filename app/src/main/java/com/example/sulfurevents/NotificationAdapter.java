package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface NotificationActionListener {
        void onAccept(@NonNull NotificationModel notification);
        void onDecline(@NonNull NotificationModel notification);
    }

    private final List<NotificationModel> notifications;
    private final NotificationActionListener listener;

    public NotificationAdapter(List<NotificationModel> notifications, NotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NotificationModel n = notifications.get(position);
        holder.tvMessage.setText(n.getMessage());

        holder.btnAccept.setVisibility(View.GONE);
        holder.btnDecline.setVisibility(View.GONE);

        if ("chosen".equals(n.getType()) && !n.isHandled()) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);

            holder.btnAccept.setOnClickListener(v -> listener.onAccept(n));
            holder.btnDecline.setOnClickListener(v -> listener.onDecline(n));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMessage;
        Button btnAccept, btnDecline;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
