package com.example.sulfurevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EntrantEventsAdapter extends RecyclerView.Adapter<EntrantEventsAdapter.EventViewHolder> {

    private Context context;
    private List<EventModel> events;
    private FirebaseFirestore db;

    // still using a fixed doc name for now
    private static final String TEMP_DOC_ID = "tempUser";

    public EntrantEventsAdapter(Context context, List<EventModel> events) {
        this.context = context;
        this.events = events;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.entrant_event_row, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = events.get(position);

        holder.tvEventName.setText(event.getEventName());
        holder.tvEventStatus.setText(event.getStatus());
        holder.tvWaitingCount.setText("Entrants: ..."); // temporary while loading

        String eventId = event.getEventId();
        if (eventId == null) {
            holder.btnJoin.setEnabled(false);
            holder.btnLeave.setEnabled(false);
            holder.tvWaitingCount.setText("Entrants: 0");
            return;
        }

        // path to this user's doc
        DocumentReference joinDoc = db.collection("events")
                .document(eventId)
                .collection("waiting_list")
                .document(TEMP_DOC_ID);

        // 1) load total entrants
        db.collection("events")
                .document(eventId)
                .collection("waiting_list")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    holder.tvWaitingCount.setText("Entrants: " + count);
                })
                .addOnFailureListener(e -> {
                    holder.tvWaitingCount.setText("Entrants: 0");
                });

        // 2) check if THIS test user is in the list to set button visibility
        joinDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                holder.btnJoin.setVisibility(View.GONE);
                holder.btnLeave.setVisibility(View.VISIBLE);
            } else {
                holder.btnJoin.setVisibility(View.VISIBLE);
                holder.btnLeave.setVisibility(View.GONE);
            }
        });

        // 3) JOIN
        holder.btnJoin.setOnClickListener(v -> {
            WaitingListEntry entry = new WaitingListEntry(Timestamp.now());
            joinDoc.set(entry)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Joined waiting list", Toast.LENGTH_SHORT).show();
                        holder.btnJoin.setVisibility(View.GONE);
                        holder.btnLeave.setVisibility(View.VISIBLE);

                        // bump the number shown
                        String current = holder.tvWaitingCount.getText().toString(); // "Entrants: X"
                        int c = extractCount(current);
                        holder.tvWaitingCount.setText("Entrants: " + (c + 1));
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Join failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // 4) LEAVE
        holder.btnLeave.setOnClickListener(v -> {
            joinDoc.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Left waiting list", Toast.LENGTH_SHORT).show();
                        holder.btnLeave.setVisibility(View.GONE);
                        holder.btnJoin.setVisibility(View.VISIBLE);

                        // lower the number shown (not below 0)
                        String current = holder.tvWaitingCount.getText().toString();
                        int c = extractCount(current);
                        if (c > 0) c--;
                        holder.tvWaitingCount.setText("Entrants: " + c);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Could not leave: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // helper to pull the number out of "Entrants: 5"
    private int extractCount(String text) {
        try {
            return Integer.parseInt(text.replace("Entrants:", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventStatus, tvWaitingCount;
        Button btnJoin, btnLeave;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventStatus = itemView.findViewById(R.id.tv_event_status);
            tvWaitingCount = itemView.findViewById(R.id.tv_waiting_count);
            btnJoin = itemView.findViewById(R.id.btn_join_waiting_list);
            btnLeave = itemView.findViewById(R.id.btn_leave_waiting_list);
        }
    }
}
