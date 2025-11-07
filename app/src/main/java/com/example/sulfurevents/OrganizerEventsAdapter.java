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
 * Adapter for displaying the organizer’s events in a RecyclerView.
 * Each card shows event info and a button to view full details.
 */
public class OrganizerEventsAdapter extends RecyclerView.Adapter<OrganizerEventsAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<OrganizerEvent> organizerEvents;

    public OrganizerEventsAdapter(Context context, ArrayList<OrganizerEvent> organizerEvents) {
        this.context = context;
        this.organizerEvents = organizerEvents;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.organizer_events_row, parent, false);
        return new MyViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return organizerEvents.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView EventName, Date, Location, Capacity;
        Button ViewDetailsButton;

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
