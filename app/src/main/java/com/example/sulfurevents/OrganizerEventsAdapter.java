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


// Done Java docs for Part 3
/**
 * Adapter for displaying the organizer’s events in a RecyclerView.
 * Each card shows event info and a button to view full details.
 */
public class OrganizerEventsAdapter extends RecyclerView.Adapter<OrganizerEventsAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<OrganizerEvent> organizerEvents;

    /**
     * Constructs the adapter for displaying organizer events in a RecyclerView.
     *
     * @param context          The calling context.
     * @param organizerEvents  The list of events to display.
     */
    public OrganizerEventsAdapter(Context context, ArrayList<OrganizerEvent> organizerEvents) {
        this.context = context;
        this.organizerEvents = organizerEvents;
    }

    /**
     * Inflates the layout for each event row and creates a ViewHolder to hold its views.
     *
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The type of view (unused here).
     * @return A new ViewHolder containing the inflated row layout.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.organizer_events_row, parent, false);
        return new MyViewHolder(view);
    }


    /**
     * Binds event data to the row UI elements at the specified position.
     *
     * @param holder   The ViewHolder containing the row's views.
     * @param position The position of the event in the list.
     *
     * Sets event name, date, location, and capacity. Also attaches a click listener
     * to open the event detail screen.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrganizerEvent event = organizerEvents.get(position);

        //  Clean, readable event info
        holder.EventName.setText(event.getEventName());
        holder.Date.setText("Start date: " + event.getStartDate());
        holder.Location.setText("Location: " + event.getLocation());

        String capacity = event.getLimitGuests();
        holder.Capacity.setText(
                (capacity == null || capacity.isBlank() || capacity.isEmpty())
                        ? "Capacity: N/A"
                        : "Capacity: " + capacity
        );

        //  Button now opens OrganizerViewEventActivity
        holder.ViewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizerViewEventActivity.class);
            intent.putExtra("eventId", event.getEventId());
            context.startActivity(intent);
        });
    }

    /**
     * Returns the number of events in the list.
     *
     * @return Total count of organizer events.
     */
    @Override
    public int getItemCount() {
        return organizerEvents.size();
    }


    /**
     * ViewHolder class that holds references to the UI elements in a single event row.
     * Used to efficiently bind data to RecyclerView rows.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView EventName, Date, Location, Capacity;
        Button ViewDetailsButton;


        /**
         * Initializes view references for the row layout.
         *
         * @param itemView The root view of the row layout.
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            EventName = itemView.findViewById(R.id.EventNameCard);
            Date = itemView.findViewById(R.id.DateDetailsCard);
            Location = itemView.findViewById(R.id.LocationDetailsCard);
            Capacity = itemView.findViewById(R.id.CapacityDetailsCard);
            ViewDetailsButton = itemView.findViewById(R.id.EditEventButtonCard); // same ID, but it's the "View Details" button
        }
    }
}