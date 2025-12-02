package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter responsible for displaying entrant-facing notifications
 * such as “you were selected” and “you were not selected”.
 * <p>
 * This adapter binds {@link NotificationItem} instances to the
 * {@code item_notification} layout and exposes user actions (accept/decline/view)
 * through the {@link NotificationActionListener} interface so that the hosting
 * activity (e.g. {@code NotificationsActivity}) can perform the corresponding
 * Firestore updates.
 *
 * <p>
 * It directly supports the UI portion of:
 * <ul>
 *     <li>US 01.04.01 – show a notification when entrant is chosen</li>
 *     <li>US 01.04.02 – show a notification when entrant is not chosen</li>
 *     <li>US 01.05.02 – allow entrant to accept an invitation</li>
 *     <li>US 01.05.03 – allow entrant to decline an invitation</li>
 * </ul>
 * <p>Author: sulfur (CMPUT 301-Part 3)</p>
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifVH> {

    /**
     * Listener used by the hosting component to react to user actions on a
     * specific notification item (accept, decline, or view details).
     */
    public interface NotificationActionListener {
        /**
         * Called when the user taps the “Accept” button on an invitation notification.
         *
         * @param item the notification that was accepted
         */
        void onAccept(NotificationItem item);

        /**
         * Called when the user taps the “Decline” button on an invitation notification.
         *
         * @param item the notification that was declined
         */
        void onDecline(NotificationItem item);

        /**
         * Called when the user requests to view details for the event referenced
         * by this notification.
         *
         * @param item the notification whose event should be shown
         */
        void onViewEvent(NotificationItem item);
    }

    private final List<NotificationItem> data;
    private final NotificationActionListener listener;

    /**
     * Constructs a new adapter for displaying notifications.
     *
     * @param data     list of notifications to display, typically loaded from Firestore
     * @param listener callback that will handle user actions on each row
     */
    public NotificationsAdapter(List<NotificationItem> data, NotificationActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    /**
     * Inflates the notification row layout and creates a new {@link NotifVH}
     * to hold its views. This is called by the RecyclerView when it needs
     * a new list item to be displayed on screen.
     *
     * @param parent  the parent ViewGroup into which the new view will be inserted
     * @param viewType ignored here because the adapter uses only one view type
     * @return a fully constructed {@link NotifVH} ready for binding
     */
    @NonNull
    @Override
    public NotifVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotifVH(v);
    }

    /**
     * Binds a {@link NotificationItem} to the provided {@link NotifVH} ViewHolder.
     * <p>
     * This method:
     * <ul>
     *     <li>Shows the event name, message, and timestamp</li>
     *     <li>Displays Accept/Decline buttons only for INVITED notifications</li>
     *     <li>Grey-outs the row if the notification has been marked as read</li>
     *     <li>Invokes callbacks on the attached {@link NotificationActionListener}
     *         so the hosting activity can update Firestore accordingly</li>
     * </ul>
     *
     * @param h        the ViewHolder whose views will be populated
     * @param position the list position of the notification being bound
     */
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

    /**
     * Returns the number of notifications in the current dataset.
     *
     * @return total count of {@link NotificationItem} objects rendered by the adapter
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * ViewHolder that holds references to the notification item views.
     * Each instance corresponds to one row in the notifications list.
     */
    static class NotifVH extends RecyclerView.ViewHolder {
        TextView tvEventName, tvMessage, tvTimestamp;
        Button btnAccept, btnDecline;

        /**
         * Creates a new ViewHolder and binds the subviews that will be populated
         * by {@link #onBindViewHolder(NotifVH, int)}.
         *
         * @param itemView the inflated row view
         */
        public NotifVH(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}