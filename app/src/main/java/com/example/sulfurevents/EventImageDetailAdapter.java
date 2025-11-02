// EventImageDetailAdapter
// Displays each image for an event in a ListView and handles delete clicks (placeholder toast for now).

package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class EventImageDetailAdapter extends ArrayAdapter<Integer> {

    public EventImageDetailAdapter(Context context, List<Integer> imageList) {
        super(context, 0, imageList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_event_image, parent, false);
        }

        Integer imageResId = getItem(position);

        ImageView imgEventImage = convertView.findViewById(R.id.imgEventImage);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteEventImage);

        imgEventImage.setImageResource(imageResId);

        btnDelete.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Deleted image " + (position + 1),
                        Toast.LENGTH_SHORT).show()
        );

        return convertView;
    }
}
