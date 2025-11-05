// AdminEventsListAdapter
// This file creates a custom list adapter for showing events in the admin panel
// It shows event name, organizer email, status, and has a delete button

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * This class defines a custom list adapter for showing events in the admin panel.
 * Each list item shows the event name, organizer email, and includes a delete button.
 */
public class AdminEventsListAdapter extends ArrayAdapter<EventModel> {

    /**
     * Constructor for creating a new AdminEventsListAdapter
     * @param context The current context
     * @param events The list of events to display
     */
    public AdminEventsListAdapter(Context context, List<EventModel> events) {
        super(context, 0, events);
    }

    /**
     * Gets the view for a single event item in the list
     * @param position The position of the event in the list
     * @param convertView The recycled view to reuse
     * @param parent The parent view group
     * @return The completed list item view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_event, parent, false);
        }

        EventModel event = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvEventName);
        TextView tvEmail = convertView.findViewById(R.id.tvOrganizerEmail);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteEvent);

        tvName.setText(event.getEventName());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());

        btnDelete.setOnClickListener(v -> {
            if (getContext() instanceof AdminEventsActivity) {
                ((AdminEventsActivity) getContext()).deleteEvent(event.getEventId());
            }
        });

        return convertView;
    }
}
