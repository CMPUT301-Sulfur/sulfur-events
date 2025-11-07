//EventAdapter
package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * This specific class shows a list of joinable events to an entrant
 * It displays the event's name, description, date range, location and capacity.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventModel> eventList;
    private final Context context;

    /**
     * Creates a new adapter for entrant events.
     *
     * @param eventList the list of events retrieved from Firestore
     * @param context   activity or fragment context used to start intents
     */
    public EventAdapter(List<EventModel> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    /**
     * Inflates the single event row layout.
     *
     * @param parent   parent view group
     * @param viewType ignored here (only one view type)
     * @return a view holder wrapping the row
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_event_row, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds one {@link EventModel} to the row: fills in text fields
     * and wires up click listeners to open {@link EventDetailsActivity}.
     *
     * @param holder   the view holder to bind
     * @param position index of the event in {@code eventList}
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);

        // ✅ Load real data from Firestore model instead of hardcoded values
        holder.eventName.setText(event.getEventName() != null ? event.getEventName() : "Unnamed Event");

        String description = event.getDescription() != null ? event.getDescription() : "No description available";
        holder.eventDetails.setText("Details: " + description);

        // ✅ Real Firestore fields
        String startDate = event.getStartDate() != null ? event.getStartDate() : "N/A";
        String endDate = event.getEndDate() != null ? event.getEndDate() : "N/A";
        holder.date.setText("Date: " + startDate + " → " + endDate);

        String location = event.getLocation() != null ? event.getLocation() : "Not specified";
        holder.location.setText("Location: " + location);

        String capacity = event.getLimitGuests() != null ? event.getLimitGuests() : "Not set";
        holder.capacity.setText("Capacity: " + capacity);

        // ✅ Clicking “Join Waiting List” button
        holder.joinButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("organizerEmail", event.getOrganizerEmail());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("startDate", event.getStartDate());
            intent.putExtra("endDate", event.getEndDate());
            intent.putExtra("capacity", event.getLimitGuests());
            context.startActivity(intent);
        });

        // ✅ Clicking the whole card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("organizerEmail", event.getOrganizerEmail());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("startDate", event.getStartDate());
            intent.putExtra("endDate", event.getEndDate());
            intent.putExtra("capacity", event.getLimitGuests());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDetails, date, location, capacity;
        Button joinButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDetails = itemView.findViewById(R.id.event_details);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            capacity = itemView.findViewById(R.id.capacity);
            joinButton = itemView.findViewById(R.id.join_button);
        }
    }
}
