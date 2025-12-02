package com.example.sulfurevents;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for EventHistoryAdapter logic.
 * Tests item count and helper methods without requiring Android framework.
 *
 * NOTE: These are simple unit tests. Add this dependency to build.gradle:
 * testImplementation 'junit:junit:4.13.2'
 *
 * For full UI testing with ViewHolders, use Robolectric:
 * testImplementation 'org.robolectric:robolectric:4.11.1'
 */
public class EventHistoryAdapterTest {

    private List<NotificationItem> testItems;
    private EventHistoryAdapter adapter;

    @Before
    public void setup() {
        testItems = new ArrayList<>();

        // Create test notification items
        NotificationItem item1 = new NotificationItem();
        item1.eventName = "Tech Conference 2025";
        item1.type = "INVITED";
        item1.message = "You have been selected for the event";
        item1.timestamp = 1704067200000L; // Jan 1, 2024
        item1.read = false;
        testItems.add(item1);

        NotificationItem item2 = new NotificationItem();
        item2.eventName = "Music Festival";
        item2.type = "NOT_SELECTED";
        item2.message = "Unfortunately, you were not selected";
        item2.timestamp = 1704153600000L; // Jan 2, 2024
        item2.read = true;
        testItems.add(item2);

        NotificationItem item3 = new NotificationItem();
        item3.eventName = "Art Exhibition";
        item3.type = "WAITING";
        item3.message = "You are on the waiting list";
        item3.timestamp = 1704240000000L; // Jan 3, 2024
        item3.read = false;
        testItems.add(item3);

        // Create adapter
        adapter = new EventHistoryAdapter(testItems, null);
    }

    /**
     * Test 1: Adapter returns correct item count
     */
    @Test
    public void testAdapterItemCount() {
        assertEquals("Adapter should have 3 items", 3, adapter.getItemCount());

        // Test with empty list
        EventHistoryAdapter emptyAdapter = new EventHistoryAdapter(new ArrayList<>(), null);
        assertEquals("Empty adapter should have 0 items", 0, emptyAdapter.getItemCount());

        // Test with single item
        List<NotificationItem> singleItem = new ArrayList<>();
        singleItem.add(testItems.get(0));
        EventHistoryAdapter singleAdapter = new EventHistoryAdapter(singleItem, null);
        assertEquals("Single item adapter should have 1 item", 1, singleAdapter.getItemCount());
    }

    /**
     * Test 2: Notification items contain correct data
     */
    @Test
    public void testNotificationItemData() {
        // Verify first item (INVITED)
        NotificationItem item1 = testItems.get(0);
        assertNotNull("First item should not be null", item1);
        assertEquals("Event name should match", "Tech Conference 2025", item1.eventName);
        assertEquals("Type should be INVITED", "INVITED", item1.type);
        assertEquals("Message should match", "You have been selected for the event", item1.message);
        assertEquals("Read status should be false", false, item1.read);

        // Verify second item (NOT_SELECTED)
        NotificationItem item2 = testItems.get(1);
        assertNotNull("Second item should not be null", item2);
        assertEquals("Event name should match", "Music Festival", item2.eventName);
        assertEquals("Type should be NOT_SELECTED", "NOT_SELECTED", item2.type);
        assertEquals("Read status should be true", true, item2.read);

        // Verify third item (WAITING)
        NotificationItem item3 = testItems.get(2);
        assertNotNull("Third item should not be null", item3);
        assertEquals("Event name should match", "Art Exhibition", item3.eventName);
        assertEquals("Type should be WAITING", "WAITING", item3.type);
        assertEquals("Read status should be false", false, item3.read);
    }

    /**
     * Test 3: Adapter handles edge cases properly
     */
    @Test
    public void testAdapterEdgeCases() {
        // Test with null values in items
        NotificationItem nullItem = new NotificationItem();
        nullItem.eventName = null;
        nullItem.type = null;
        nullItem.message = null;
        nullItem.timestamp = 0;
        nullItem.read = false;

        List<NotificationItem> edgeCaseItems = new ArrayList<>();
        edgeCaseItems.add(nullItem);

        EventHistoryAdapter edgeAdapter = new EventHistoryAdapter(edgeCaseItems, null);
        assertEquals("Adapter should handle null values", 1, edgeAdapter.getItemCount());

        // Test with multiple items of same type
        NotificationItem item1 = new NotificationItem();
        item1.eventName = "Event 1";
        item1.type = "INVITED";
        item1.timestamp = System.currentTimeMillis();
        item1.read = false;

        NotificationItem item2 = new NotificationItem();
        item2.eventName = "Event 2";
        item2.type = "INVITED";
        item2.timestamp = System.currentTimeMillis();
        item2.read = false;

        List<NotificationItem> sameTypeItems = new ArrayList<>();
        sameTypeItems.add(item1);
        sameTypeItems.add(item2);

        EventHistoryAdapter sameTypeAdapter = new EventHistoryAdapter(sameTypeItems, null);
        assertEquals("Adapter should handle multiple items of same type", 2, sameTypeAdapter.getItemCount());

        // Test adapter with null listener (should not crash)
        EventHistoryAdapter nullListenerAdapter = new EventHistoryAdapter(testItems, null);
        assertNotNull("Adapter should be created with null listener", nullListenerAdapter);
        assertEquals("Adapter with null listener should have correct count", 3, nullListenerAdapter.getItemCount());
    }
}
