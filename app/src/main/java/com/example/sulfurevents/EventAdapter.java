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


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {


    private List<EventModel> eventList;
    private Context context;


    public EventAdapter(List<EventModel> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_event_row, parent, false);
        return new EventViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);


        holder.eventName.setText(event.getEventName());
        holder.eventDetails.setText("Details: " + (event.getDescription() != null ? event.getDescription() : "N/A"));


        // You'll need to add these fields to EventModel or display what's available
        holder.date.setText("Date: TBD");
        holder.location.setText("Location: TBD");
        holder.capacity.setText("Capacity: TBD");


        holder.joinButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("organizerEmail", event.getOrganizerEmail());
            context.startActivity(intent);
        });


        // Make the entire card clickable
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("organizerEmail", event.getOrganizerEmail());
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
