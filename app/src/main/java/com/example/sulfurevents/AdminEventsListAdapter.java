// this file creates a custom list adapter for showing events in the admin panel
// it shows event name, organizer email, status, and has a delete button

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdminEventsListAdapter extends ArrayAdapter<EventModel> {

    // constructor takes the app context and list of events
    public AdminEventsListAdapter(Context context, List<EventModel> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // check if the view is already created, if not inflate a new one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_event, parent, false);
        }

        // get the current event object
        EventModel event = getItem(position);

        // find the text views and button in the layout
        TextView tvName = convertView.findViewById(R.id.tvEventName);
        TextView tvEmail = convertView.findViewById(R.id.tvOrganizerEmail);
        TextView tvStatus = convertView.findViewById(R.id.tvEventStatus);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteEvent);

        // set the text for name, email, and status
        tvName.setText(event.getEventName());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());
        tvStatus.setText("Status: " + event.getStatus());

        // show a toast message when delete button is clicked
        btnDelete.setOnClickListener(v ->
                Toast.makeText(getContext(), "Delete " + event.getEventName(), Toast.LENGTH_SHORT).show()
        );

        // return the completed view for display
        return convertView;
    }
}
