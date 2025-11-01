package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrganizerEventsAdapter extends RecyclerView.Adapter<OrganizerEventsAdapter.MyViewHolder> {

    Context context;
    ArrayList<OrganizerEvents> OrganizerEvent;

    public OrganizerEventsAdapter(Context context, ArrayList<OrganizerEvents> OrganizerEvent){

        this.context = context;
        this.OrganizerEvent = OrganizerEvent;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This is where you inflate the layout (giving a look to our rows)

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.organizer_events_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Assigning Values to the view we create in the recycler_view_row layout file
        // based on the position of the recyclerView
        holder.EventName.setText(OrganizerEvent.get(position).getEventName());
        holder.Date.setText(OrganizerEvent.get(position).getDate());
        //holder.CurrentCapacity.setText(OrganizerEvent.get(position).getCurrentCapacity()); // want capacity to be displayed
        holder.Location.setText(OrganizerEvent.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        // The recycler view just wants to know the number of items you want displayed
        return OrganizerEvent.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        // grabbing the views from our recycler_view_row layout file
        // Kinda like in the onCreate method

        TextView EventName, Date, Location;
        //TextView CurrentCapacity

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            EventName = itemView.findViewById(R.id.EventNameCard);
            Date = itemView.findViewById(R.id.DateDetailsCard);
            Location = itemView.findViewById(R.id.LocationDetailsCard);
            //CurrentCapacity = itemView.findViewById(R.id.CapacityDetailsCard);

        }
    }

}
