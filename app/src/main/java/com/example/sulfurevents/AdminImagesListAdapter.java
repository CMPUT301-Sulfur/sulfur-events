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

// Adapter for displaying events (with images) in AdminImagesActivity
public class AdminImagesListAdapter extends ArrayAdapter<EventModel> {

    public AdminImagesListAdapter(Context context, List<EventModel> events) {
        super(context, 0, events);
    }

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

            // Load image from Firestore URL
            Glide.with(getContext())
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgThumbnail);

            // Handle "View/Delete Images" button click
            btnManage.setOnClickListener(v -> {
                if (getContext() instanceof AdminImagesActivity) {
                    ((AdminImagesActivity) getContext()).openEventImageDetail(event);
                }
            });
        }

        return convertView;
    }
}