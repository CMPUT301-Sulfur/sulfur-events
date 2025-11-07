package com.example.sulfurevents;

import android.os.Bundle;
import android.view.View;
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
 * US 02.06.03 – View final enrolled list
 *
 * <p>Organizer-facing screen that displays the final list of enrolled entrants for a given event.
 * This reads the event's {@code enrolled_list} from Firestore, resolves each deviceId to an
 * optional profile document in {@code Profiles/{deviceId}}, and shows a simple read-only list.</p>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>This screen is strictly read-only: no mutations occur here.</li>
 *   <li>It reuses the existing {@link OrganizerWaitlistAdapter} and the same row layout in order to
 *       keep code surface small. Any selection state in the adapter is ignored on this screen.</li>
 * </ul>
 *
 * <h3>Firestore</h3>
 * <ul>
 *   <li>{@code Events/{eventId}/enrolled_list} : List&lt;String&gt; of deviceIds</li>
 *   <li>{@code Profiles/{deviceId}}           : optional profile details (name/email) for display</li>
 * </ul>
 *
 * <p>Author: sulfur — CMPUT 301 (Part 3)</p>
 */
public class OrganizerEnrolledActivity extends AppCompatActivity {

    /** Firestore client. */
    private FirebaseFirestore db;
    /** Event document id whose enrolled list we show. */
    private String eventId;

    // UI
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // Data shown in the adapter (parallel lists)
    private OrganizerWaitlistAdapter adapter; // reused, selection ignored here
    private final List<User> users = new ArrayList<>();
    private final List<String> deviceIds = new ArrayList<>();

    /**
     * Lifecycle entry: binds views, wires adapter, and triggers initial load.
     *
     * @param savedInstanceState standard Android saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_enrolled_activity);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        // Back arrow
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvEnrolled);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Reuse WaitlistAdapter; row layout is compatible and simpler to maintain.
        adapter = new OrganizerWaitlistAdapter(users, deviceIds);
        recyclerView.setAdapter(adapter);

        loadEnrolled();
    }

    /**
     * Loads the {@code enrolled_list} for this event and kicks off profile resolution.
     * Shows a simple empty state if nothing is enrolled yet.
     * <p>
     * Tolerates alternate field names (e.g., {@code enrolledList}, {@code final_list}, {@code finalList})
     * to be robust against teammate variations.
     * </p>
     */
    private void loadEnrolled() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        users.clear();
        deviceIds.clear();
        adapter.notifyDataSetChanged(); // clear existing rows while loading

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    // Tolerate alternate field names if teammates used camelCase or different keys
                    List<String> enrolled = getStringList(doc, "enrolled_list", "enrolledList", "final_list", "finalList");
                    handleEnrolled(enrolled);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load enrolled list.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Best-effort retrieval of a string list field that may appear under multiple keys.
     * Always returns a non-null list (empty when absent/unexpected type).
     *
     * @param doc  Firestore document snapshot
     * @param keys Fallback field names in priority order
     * @return non-null list instance
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
     * After we have the {@code enrolled_list} of deviceIds, resolve each to a profile (if any)
     * and then update the adapter.
     *
     * @param ids list of device IDs currently enrolled for the event
     */
    private void handleEnrolled(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No enrolled entrants.");
            return;
        }

        deviceIds.addAll(ids);

        final int total = ids.size();
        final int[] done = {0}; // async completion counter

        // Resolve each profile independently; render when all loads complete.
        for (String id : ids) {
            db.collection("Profiles").document(id).get()
                    .addOnSuccessListener(profileDoc -> {
                        users.add(extractUser(id, profileDoc));
                        if (++done[0] == total) finishPopulate();
                    })
                    .addOnFailureListener(e -> {
                        // Even if profile fetch fails, show a placeholder entry for the deviceId.
                        users.add(extractUser(id, null));
                        if (++done[0] == total) finishPopulate();
                    });
        }
    }

    /**
     * Converts a profile document (if present) to a {@link User} for display and
     * falls back to a placeholder name if profile data is missing/invalid.
     *
     * @param id  deviceId key used in {@code Profiles}
     * @param doc profile snapshot (may be null/absent)
     * @return non-null {@code User} object suitable for display
     */
    private User extractUser(String id, DocumentSnapshot doc) {
        User u = new User();
        if (doc != null && doc.exists()) {
            try {
                User fromDb = doc.toObject(User.class);
                if (fromDb != null) u = fromDb;
            } catch (Exception ignored) {
                // Ignore mapping errors; fall back to default user
            }
        }
        if (u.getName() == null || u.getName().isEmpty()) {
            u.setName("(Unnamed)");
        }
        return u;
    }

    /**
     * Finalizes population: hide progress, toggle empty state, and refresh the adapter.
     */
    private void finishPopulate() {
        progressBar.setVisibility(View.GONE);
        if (users.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No enrolled entrants.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}

