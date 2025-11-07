package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * {@code OrganizerEventsAdapter}
 * <p>
 * RecyclerView adapter used by the organizer’s dashboard to display all events created by the organizer.
 * Each event card includes essential information (name, date, location, capacity)
 * and provides a button to view or manage detailed event data.
 * </p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Inflates each event card layout from {@code organizer_events_row.xml}.</li>
 *   <li>Binds event metadata such as name, start date, location, and capacity.</li>
 *   <li>Handles click navigation to {@link OrganizerViewEventActivity} for event management.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * OrganizerEventsAdapter adapter = new OrganizerEventsAdapter(context, eventList);
 * recyclerView.setAdapter(adapter);
 * </pre>
 *
 * <p>Author: sulfur — CMPUT 301 (Part 3)</p>
 */
public class OrganizerEventsAdapter extends RecyclerView.Adapter<OrganizerEventsAdapter.MyViewHolder> {

    /** Parent context for inflating layouts and launching new activities. */
    private final Context context;

    /** List of organizer-created events displayed in the RecyclerView. */
    private final ArrayList<OrganizerEvent> organizerEvents;

    /**
     * Constructs an adapter for displaying organizer events.
     *
     * @param context          the parent context (typically an Activity)
     * @param organizerEvents  the list of events to render
     */
    public OrganizerEventsAdapter(Context context, ArrayList<OrganizerEvent> organizerEvents) {
        this.context = context;
        this.organizerEvents = organizerEvents;
    }

    /**
     * Inflates a new event row from XML layout when needed.
     *
     * @param parent   parent ViewGroup
     * @param viewType item view type (not used here)
     * @return new ViewHolder instance for this event row
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.organizer_events_row, parent, false);
        return new MyViewHolder(view);
    }

    /**
     * Binds event details to the UI components within each card.
     *
     * @param holder   the ViewHolder containing the row layout
     * @param position the index of the current event in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrganizerEvent event = organizerEvents.get(position);

        // --- Populate basic event info ---
        holder.EventName.setText(event.getEventName());
        holder.Date.setText("Start date: " + event.getStartDate());
        holder.Location.setText("Location: " + event.getLocation());

        String capacity = event.getLimitGuests();
        holder.Capacity.setText(
                (capacity == null || capacity.isBlank() || capacity.isEmpty())
                        ? "Capacity: N/A"
                        : "Capacity: " + capacity
        );

        // --- Button click navigates to OrganizerViewEventActivity ---
        holder.ViewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizerViewEventActivity.class);
            intent.putExtra("eventId", event.getEventId());
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of events displayed.
     *
     * @return number of organizer events
     */
    @Override
    public int getItemCount() {
        return organizerEvents.size();
    }

    /**
     * {@code MyViewHolder}
     * <p>
     * Inner static ViewHolder class that caches references to TextViews and the View Details button.
     * Reduces unnecessary {@code findViewById()} calls for smoother RecyclerView scrolling.
     * </p>
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView EventName, Date, Location, Capacity;
        Button ViewDetailsButton;

        /**
         * Binds all UI elements of a single event card.
         *
         * @param itemView the inflated card view
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            EventName = itemView.findViewById(R.id.EventNameCard);
            Date = itemView.findViewById(R.id.DateDetailsCard);
            Location = itemView.findViewById(R.id.LocationDetailsCard);
            Capacity = itemView.findViewById(R.id.CapacityDetailsCard);
            // The button shares the "EditEventButtonCard" ID but acts as "View Details"
            ViewDetailsButton = itemView.findViewById(R.id.EditEventButtonCard);
        }
    }
}
