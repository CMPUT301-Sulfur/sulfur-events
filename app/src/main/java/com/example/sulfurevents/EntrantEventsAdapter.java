package com.example.sulfurevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntrantEventsAdapter extends RecyclerView.Adapter<EntrantEventsAdapter.EventViewHolder> {

    public interface OnJoinClickListener {
        void onJoinClicked(EventModel event);
        void onLeaveClicked(EventModel event);
    }

    private final List<EventModel> events;
    private final OnJoinClickListener listener;

    public EntrantEventsAdapter(List<EventModel> events, OnJoinClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_row_joinable_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = events.get(position);

        holder.title.setText(nonNull(event.getEventName()));
        holder.date.setText(nonNull(event.getOrganizerEmail()));
        holder.location.setText(nonNull(event.getStatus()));

        // show/hide correct button
        if (event.isJoinedByCurrentUser()) {
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnLeave.setVisibility(View.VISIBLE);
        } else {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnLeave.setVisibility(View.GONE);
        }

        holder.btnJoin.setOnClickListener(v -> {
            if (listener != null) listener.onJoinClicked(event);
        });

        holder.btnLeave.setOnClickListener(v -> {
            if (listener != null) listener.onLeaveClicked(event);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, location;
        Button btnJoin, btnLeave;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txt_event_title);
            date = itemView.findViewById(R.id.txt_event_date);
            location = itemView.findViewById(R.id.txt_event_location);
            btnJoin = itemView.findViewById(R.id.btn_join_waiting);
            btnLeave = itemView.findViewById(R.id.btn_leave_waiting);
        }
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }
}
