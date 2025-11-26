package com.example.sulfurevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.06.02 – View list of cancelled entrants.
 *
 * Organizer-facing screen that displays the list of cancelled entrants for a given event.
 * Reads the event's {@code cancelled_list} from Firestore, resolves each deviceId to an
 * optional profile document in {@code Profiles/{deviceId}}, and shows a simple read-only list.
 */
public class OrganizerCancelledActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    // UI
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // Data shown in the adapter (parallel lists)
    private OrganizerWaitlistAdapter adapter; // reused, selection ignored here
    private final List<User> users = new ArrayList<>();
    private final List<String> deviceIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_cancelled_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Back arrow
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvCancelled);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Reuse OrganizerWaitlistAdapter, same row layout as waitlist.
        adapter = new OrganizerWaitlistAdapter(users, deviceIds);
        recyclerView.setAdapter(adapter);

        loadCancelled();
    }

    /**
     * Loads the {@code cancelled_list} for this event and kicks off profile resolution.
     * Shows a simple empty state if nothing is cancelled yet.
     */
    private void loadCancelled() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        emptyText.setVisibility(TextView.GONE);
        users.clear();
        deviceIds.clear();
        adapter.notifyDataSetChanged();

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    // Allow for possible alternate field names if teammates used camelCase
                    List<String> cancelled = getStringList(doc, "cancelled_list", "cancelledList");
                    handleCancelled(cancelled);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    emptyText.setVisibility(TextView.VISIBLE);
                    emptyText.setText("Failed to load cancelled list.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Best-effort retrieval of a string list field that may appear under multiple keys,
     * returning a non-null list even when the field is absent or of an unexpected type.
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(DocumentSnapshot doc, String... keys) {
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof List) {
                try {
                    return (List<String>) v;
                } catch (ClassCastException ignored) {
                    // fall through to return empty list
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * After we have the {@code cancelled_list} of deviceIds, resolve each to a profile (if any)
     * and then update the adapter.
     */
    private void handleCancelled(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            progressBar.setVisibility(ProgressBar.GONE);
            emptyText.setVisibility(TextView.VISIBLE);
            emptyText.setText("No cancelled entrants.");
            return;
        }

        deviceIds.addAll(ids);

        final int total = ids.size();
        final int[] done = {0};

        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        users.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        users.add(extractUser(id, null)); // fallback to placeholder
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a profile document (if present) to a {@link User} for display.
     * Falls back to a placeholder name if profile data is missing/invalid.
     */
    private User extractUser(String id, DocumentSnapshot doc) {
        User u = new User();
        if (doc != null && doc.exists()) {
            try {
                User fromDb = doc.toObject(User.class);
                if (fromDb != null) u = fromDb;
            } catch (Exception ignored) {
                // keep default
            }
        }
        if (u.getName() == null || u.getName().isEmpty()) {
            u.setName("(Unnamed)");
        }
        // adapter will display them accordingly.
        return u;
    }

    /**
     * Finalizes population: hide progress, set empty state visibility,
     * and notify the adapter to render the rows.
     */
    private void finishPopulate() {
        progressBar.setVisibility(ProgressBar.GONE);
        if (users.isEmpty()) {
            emptyText.setVisibility(TextView.VISIBLE);
            emptyText.setText("No cancelled entrants.");
        } else {
            emptyText.setVisibility(TextView.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}

