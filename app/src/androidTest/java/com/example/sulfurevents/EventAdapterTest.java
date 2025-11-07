package com.example.sulfurevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight test for {@link EventAdapter} that doesn't rely on a host
 * RecyclerView/Activity. We just:
 * - build the adapter with fake data
 * - create/bind a ViewHolder
 * - assert the text got bound.
 */
@RunWith(AndroidJUnit4.class)
public class EventAdapterTest {

    @Test
    public void adapter_binds_text_without_host_activity() {
        // 1) context we can inflate with
        Context context = ApplicationProvider.getApplicationContext();

        // 2) fake data
        List<EventModel> events = new ArrayList<>();
        EventModel e = new EventModel();
        e.setEventId("evt-123");
        e.setEventName("Simple Event");
        e.setDescription("Just testing");
        e.setStartDate("2025-01-01");
        e.setEndDate("2025-01-02");
        e.setLocation("Test City");
        e.setLimitGuests("25");
        events.add(e);

        // 3) build adapter
        EventAdapter adapter = new EventAdapter(events, context);

        // sanity: item count should match list size
        assertEquals(1, adapter.getItemCount());

        // 4) manually create a ViewHolder using the same inflater the adapter uses
        ViewGroup parent = new android.widget.FrameLayout(context);
        EventAdapter.EventViewHolder vh =
                adapter.onCreateViewHolder(parent, /*viewType*/ 0);

        // 5) bind our only item
        adapter.onBindViewHolder(vh, 0);

        // 6) assert some text was bound
        assertEquals("Simple Event", vh.eventName.getText().toString());
        // description is prefixed with "Details: "
        assertTrue(vh.eventDetails.getText().toString().contains("Just testing"));
        // location is prefixed with "Location: "
        assertTrue(vh.location.getText().toString().contains("Test City"));
    }
}
