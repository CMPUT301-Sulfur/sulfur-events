package com.example.sulfurevents;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for EventAdapter.
 * Tests adapter item count and event list handling without requiring UI thread.
 *
 * NOTE: This is a simple unit test that avoids UI operations.
 * Add this dependency to build.gradle:
 * testImplementation 'junit:junit:4.13.2'
 *
 * This test runs on local JVM without needing an emulator or device.
 */
public class EventAdapterTest {

    private List<EventModel> testEventList;

    @Before
    public void setup() {
        testEventList = new ArrayList<>();

        // Create test events
        EventModel event1 = new EventModel();
        event1.setEventId("event1");
        event1.setEventName("Tech Conference 2025");
        event1.setDescription("Annual technology conference");
        event1.setLocation("San Francisco");
        event1.setStartDate("01/15/2025");
        event1.setEndDate("01/17/2025");
        event1.setLimitGuests("100");
        testEventList.add(event1);

        EventModel event2 = new EventModel();
        event2.setEventId("event2");
        event2.setEventName("Music Festival");
        event2.setDescription("Summer outdoor music event");
        event2.setLocation("Los Angeles");
        event2.setStartDate("06/01/2025");
        event2.setEndDate("06/03/2025");
        event2.setLimitGuests("500");
        testEventList.add(event2);

        EventModel event3 = new EventModel();
        event3.setEventId("event3");
        event3.setEventName("Art Exhibition");
        event3.setDescription("Contemporary art showcase");
        event3.setLocation("New York");
        event3.setStartDate("03/10/2025");
        event3.setEndDate("03/20/2025");
        event3.setLimitGuests("50");
        testEventList.add(event3);
    }

    /**
     * Test: Adapter returns correct item count and handles different list sizes
     * This test does not require UI thread or Android Context
     */
    @Test
    public void testAdapterItemCount() {
        // Create adapter with null context (no UI operations)
        EventAdapter adapter = new EventAdapter(testEventList, null);

        // Test item count
        assertEquals("Adapter should have 3 items", 3, adapter.getItemCount());

        // Test with empty list
        List<EventModel> emptyList = new ArrayList<>();
        EventAdapter emptyAdapter = new EventAdapter(emptyList, null);
        assertEquals("Empty adapter should have 0 items", 0, emptyAdapter.getItemCount());

        // Test with single event
        List<EventModel> singleEventList = new ArrayList<>();
        singleEventList.add(testEventList.get(0));
        EventAdapter singleAdapter = new EventAdapter(singleEventList, null);
        assertEquals("Single event adapter should have 1 item", 1, singleAdapter.getItemCount());

        // Test with large list
        List<EventModel> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            EventModel event = new EventModel();
            event.setEventId("event" + i);
            event.setEventName("Event " + i);
            event.setDescription("Description " + i);
            event.setStartDate("01/01/2025");
            event.setEndDate("01/31/2025");
            largeList.add(event);
        }
        EventAdapter largeAdapter = new EventAdapter(largeList, null);
        assertEquals("Large adapter should have 100 items", 100, largeAdapter.getItemCount());

        // Verify the adapter was created successfully
        assertNotNull("Adapter should not be null", adapter);
        assertNotNull("Event list should not be null", testEventList);
    }

    /**
     * Test: Verify event data is properly stored in EventModel objects
     */
    @Test
    public void testEventDataIntegrity() {
        // Verify first event
        EventModel firstEvent = testEventList.get(0);
        assertEquals("First event ID should match", "event1", firstEvent.getEventId());
        assertEquals("First event name should match", "Tech Conference 2025", firstEvent.getEventName());
        assertEquals("First event location should match", "San Francisco", firstEvent.getLocation());
        assertEquals("First event capacity should match", "100", firstEvent.getLimitGuests());
        assertEquals("First event start date should match", "01/15/2025", firstEvent.getStartDate());
        assertEquals("First event end date should match", "01/17/2025", firstEvent.getEndDate());

        // Verify second event
        EventModel secondEvent = testEventList.get(1);
        assertEquals("Second event name should match", "Music Festival", secondEvent.getEventName());
        assertEquals("Second event description should match", "Summer outdoor music event", secondEvent.getDescription());
        assertEquals("Second event start date should match", "06/01/2025", secondEvent.getStartDate());
        assertEquals("Second event end date should match", "06/03/2025", secondEvent.getEndDate());

        // Verify third event
        EventModel thirdEvent = testEventList.get(2);
        assertEquals("Third event name should match", "Art Exhibition", thirdEvent.getEventName());
        assertEquals("Third event description should match", "Contemporary art showcase", thirdEvent.getDescription());
        assertEquals("Third event location should match", "New York", thirdEvent.getLocation());
        assertEquals("Third event capacity should match", "50", thirdEvent.getLimitGuests());
    }

    /**
     * Test: Handle null or missing event fields gracefully
     */
    @Test
    public void testEventWithNullFields() {
        List<EventModel> nullFieldsList = new ArrayList<>();

        // Create event with some null fields
        EventModel eventWithNulls = new EventModel();
        eventWithNulls.setEventId("event-null");
        eventWithNulls.setEventName(null);  // null name
        eventWithNulls.setDescription(null);  // null description
        eventWithNulls.setLocation("Test Location");
        eventWithNulls.setStartDate("01/01/2025");
        eventWithNulls.setEndDate("01/31/2025");
        nullFieldsList.add(eventWithNulls);

        EventAdapter adapter = new EventAdapter(nullFieldsList, null);

        // Should still return correct count
        assertEquals("Adapter with null fields should have 1 item", 1, adapter.getItemCount());

        // Verify the event exists
        assertNotNull("Event should not be null", nullFieldsList.get(0));
        assertEquals("Event ID should match", "event-null", nullFieldsList.get(0).getEventId());
    }
}
