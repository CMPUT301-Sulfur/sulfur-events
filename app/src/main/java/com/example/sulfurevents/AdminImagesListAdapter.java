// AdminImagesListAdapter
// Adapter for displaying events with images in a ListView.
// Shows thumbnail, event info, and a button to manage images.

package com.example.sulfurevents;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdminImagesListAdapter extends ArrayAdapter<ImageEventModel> {

    public AdminImagesListAdapter(Context context, List<ImageEventModel> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_image_event, parent, false);
        }

        ImageEventModel event = getItem(position);

        ImageView imgThumb = convertView.findViewById(R.id.imgEventThumbnail);
        TextView tvName = convertView.findViewById(R.id.tvImageEventName);
        TextView tvEmail = convertView.findViewById(R.id.tvImageEventEmail);
        TextView tvStatus = convertView.findViewById(R.id.tvImageEventStatus);
        Button btnManage = convertView.findViewById(R.id.btnManageEventImages);

        imgThumb.setImageResource(event.getImageResId());
        tvName.setText(event.getEventName());
        tvEmail.setText("Organizer: " + event.getOrganizerEmail());
        tvStatus.setText("Status: " + event.getStatus());

        btnManage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EventImageDetailActivity.class);
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("organizerEmail", event.getOrganizerEmail());
            getContext().startActivity(intent);
        });

        return convertView;
    }
}
