// AdminImagesListAdapter
// Adapter for displaying events with images in AdminImagesActivity.

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * This class defines a custom list adapter for showing events with images.
 * It is used in the admin images screen.
 */
public class AdminImagesListAdapter extends ArrayAdapter<EventModel> {

    /**
     * Constructor for creating a new AdminImagesListAdapter
     * @param context The current context
     * @param events The list of events to display
     */
    public AdminImagesListAdapter(Context context, List<EventModel> events) {
        super(context, 0, events);
    }

    /**
     * Gets the view for a single event item in the list
     * @param position The position of the event in the list
     * @param convertView The recycled view to reuse
     * @param parent The parent view group
     * @return The completed list item view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_image_event, parent, false);
        }

        EventModel event = getItem(position);

        ImageView imgThumbnail = convertView.findViewById(R.id.imgEventThumbnail);
        TextView tvName = convertView.findViewById(R.id.tvImageEventName);
        TextView tvEmail = convertView.findViewById(R.id.tvImageEventEmail);
        Button btnManage = convertView.findViewById(R.id.btnManageEventImages);

        if (event != null) {
            tvName.setText(event.getEventName());
            tvEmail.setText("Organizer: " + event.getOrganizerEmail());

            Glide.with(getContext())
                    .load(event.getPosterURL())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgThumbnail);

            btnManage.setOnClickListener(v -> {
                if (getContext() instanceof AdminImagesActivity) {
                    ((AdminImagesActivity) getContext()).openEventImageDetail(event);
                }
            });
        }

        return convertView;
    }
}