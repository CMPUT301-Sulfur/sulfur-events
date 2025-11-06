package com.example.sulfurevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EntrantEventsAdapter adapter;
    private final List<EventModel> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // this is your layout that has recyclerView, progressBar, tvEmpty, and the button
        View view = inflater.inflate(R.layout.entrant_activity, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantEventsAdapter(requireContext(), eventList);
        recyclerView.setAdapter(adapter);

        // hook up the lottery button (kept in the layout)
        View btnGuidelines = view.findViewById(R.id.btn_lottery_guidelines);
        if (btnGuidelines != null) {
            btnGuidelines.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), LotteryGuidelinesActivity.class);
                startActivity(i);
            });
        }

        db = FirebaseFirestore.getInstance();
        loadEventsFromFirestore();

        return view;
    }

    private void loadEventsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        // IMPORTANT: collection name matches your Firestore ("Events")
        db.collection("Events")
                .get()
                .addOnSuccessListener(query -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        EventModel event = doc.toObject(EventModel.class);
                        // store the doc id so join/leave code can use it
                        event.setEventId(doc.getId());
                        eventList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (eventList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No joinable events right now.");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load events.");
                });
    }
}
