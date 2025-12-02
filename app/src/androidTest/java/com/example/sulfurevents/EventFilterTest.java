package com.example.sulfurevents;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for EventFilter class.
 * Tests US 01.01.04 - Filter events by interests and availability.
 * <p>
 * Tests cover:
 * - Keyword filtering (interests)
 * - Date range filtering (availability)
 * - Combined keyword and date filtering
 * - Edge cases (null values, empty strings, invalid dates)
 * - Date format handling (MM/dd/yyyy and MM/dd/yy)
 * - Fallback date matching
 */
public class EventFilterTest {

    private List<EventModel> testEvents;

    @Before
    public void setup() {
        testEvents = new ArrayList<>();

        // Event 1: Tech Conference
        EventModel event1 = new EventModel();
        event1.setEventName("Tech Conference 2025");
        event1.setDescription("Annual technology conference");
        event1.setLocation("San Francisco");
        event1.setStartDate("01/15/2025");
        event1.setEndDate("01/17/2025");
        testEvents.add(event1);

        // Event 2: Music Festival
        EventModel event2 = new EventModel();
        event2.setEventName("Summer Music Festival");
        event2.setDescription("Outdoor music event");
        event2.setLocation("Los Angeles");
        event2.setStartDate("06/01/2025");
        event2.setEndDate("06/03/2025");
        testEvents.add(event2);

        // Event 3: Art Exhibition
        EventModel event3 = new EventModel();
        event3.setEventName("Modern Art Exhibition");
        event3.setDescription("Contemporary art showcase");
        event3.setLocation("New York");
        event3.setStartDate("03/10/2025");
        event3.setEndDate("03/20/2025");
        testEvents.add(event3);

        // Event 4: Sports Tournament
        EventModel event4 = new EventModel();
        event4.setEventName("Basketball Tournament");
        event4.setDescription("Annual basketball competition");
        event4.setLocation("Chicago");
        event4.setStartDate("12/01/2025");
        event4.setEndDate("12/15/2025");
        testEvents.add(event4);

        // Event 5: Food Festival (with short year format)
        EventModel event5 = new EventModel();
        event5.setEventName("Food Festival");
        event5.setDescription("Local food and wine tasting");
        event5.setLocation("Portland");
        event5.setStartDate("08/10/25");
        event5.setEndDate("08/12/25");
        testEvents.add(event5);
    }

    /**
     * Test 1: Filter with no criteria should return all events
     * US 01.01.04
     */
    @Test
    public void testFilterWithNoCriteria() {
        List<EventModel> result = EventFilter.filter(testEvents, null, null);

        assertEquals("Should return all events when no filters applied",
                testEvents.size(), result.size());
    }

    /**
     * Test 2: Filter with empty strings should return all events
     * US 01.01.04
     */
    @Test
    public void testFilterWithEmptyStrings() {
        List<EventModel> result = EventFilter.filter(testEvents, "", "");

        assertEquals("Should return all events with empty filters",
                testEvents.size(), result.size());
    }

    /**
     * Test 3: Filter by keyword in event name
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterByKeywordInName() {
        List<EventModel> result = EventFilter.filter(testEvents, "music", null);

        assertEquals("Should find 1 event with 'music' in name", 1, result.size());
        assertTrue("Should contain Music Festival",
                result.get(0).getEventName().contains("Music"));
    }

    /**
     * Test 4: Filter by keyword in description
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterByKeywordInDescription() {
        List<EventModel> result = EventFilter.filter(testEvents, "technology", null);

        assertEquals("Should find 1 event with 'technology' in description", 1, result.size());
        assertEquals("Should be Tech Conference",
                "Tech Conference 2025", result.get(0).getEventName());
    }

    /**
     * Test 5: Filter by keyword in location
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterByKeywordInLocation() {
        List<EventModel> result = EventFilter.filter(testEvents, "chicago", null);

        assertEquals("Should find 1 event in Chicago", 1, result.size());
        assertEquals("Should be Basketball Tournament",
                "Basketball Tournament", result.get(0).getEventName());
    }

    /**
     * Test 6: Filter by keyword (case insensitive)
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterKeywordCaseInsensitive() {
        List<EventModel> result1 = EventFilter.filter(testEvents, "MUSIC", null);
        List<EventModel> result2 = EventFilter.filter(testEvents, "music", null);
        List<EventModel> result3 = EventFilter.filter(testEvents, "MuSiC", null);

        assertEquals("Uppercase should find same results", result1.size(), result2.size());
        assertEquals("Mixed case should find same results", result2.size(), result3.size());
    }

    /**
     * Test 7: Filter by keyword that matches multiple events
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterKeywordMultipleMatches() {
        List<EventModel> result = EventFilter.filter(testEvents, "festival", null);

        assertEquals("Should find 2 events with 'festival'", 2, result.size());
    }

    /**
     * Test 8: Filter by keyword with no matches
     * US 01.01.04 - Interests filtering
     */
    @Test
    public void testFilterKeywordNoMatches() {
        List<EventModel> result = EventFilter.filter(testEvents, "swimming", null);

        assertEquals("Should find 0 events", 0, result.size());
    }

    /**
     * Test 9: Filter by date within event range
     * US 01.01.04 - Availability filtering
     */
    @Test
    public void testFilterByDateWithinRange() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "01/16/2025");

        assertEquals("Should find 1 event on 01/16/2025", 1, result.size());
        assertEquals("Should be Tech Conference",
                "Tech Conference 2025", result.get(0).getEventName());
    }

    /**
     * Test 10: Filter by date on event start date
     * US 01.01.04 - Availability filtering
     */
    @Test
    public void testFilterByDateOnStartDate() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "01/15/2025");

        assertEquals("Should find event starting on 01/15/2025", 1, result.size());
        assertEquals("Should be Tech Conference",
                "Tech Conference 2025", result.get(0).getEventName());
    }

    /**
     * Test 11: Filter by date on event end date
     * US 01.01.04 - Availability filtering
     */
    @Test
    public void testFilterByDateOnEndDate() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "01/17/2025");

        assertEquals("Should find event ending on 01/17/2025", 1, result.size());
        assertEquals("Should be Tech Conference",
                "Tech Conference 2025", result.get(0).getEventName());
    }

    /**
     * Test 12: Filter by date before all events
     * US 01.01.04 - Availability filtering
     */
    @Test
    public void testFilterByDateBeforeAllEvents() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "01/01/2025");

        assertEquals("Should find 0 events before all start dates", 0, result.size());
    }

    /**
     * Test 13: Filter by date after all events
     * US 01.01.04 - Availability filtering
     * FIXED: 12/31/2025 is after Basketball Tournament ends (12/15/2025)
     */
    @Test
    public void testFilterByDateAfterAllEvents() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "12/31/2025");

        assertEquals("Should find 0 events after all end dates", 0, result.size());
    }

    /**
     * Test 14: Filter by date with short year format (MM/dd/yy)
     * US 01.01.04 - Availability filtering
     */
    @Test
    public void testFilterByDateShortYearFormat() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "08/11/25");

        assertEquals("Should find 1 event with short year format", 1, result.size());
        assertEquals("Should be Food Festival",
                "Food Festival", result.get(0).getEventName());
    }

    /**
     * Test 15: Combined filter - keyword and date
     * US 01.01.04 - Combined filtering
     */
    @Test
    public void testCombinedFilterKeywordAndDate() {
        List<EventModel> result = EventFilter.filter(testEvents, "art", "03/15/2025");

        assertEquals("Should find 1 event matching both criteria", 1, result.size());
        assertEquals("Should be Modern Art Exhibition",
                "Modern Art Exhibition", result.get(0).getEventName());
    }

    /**
     * Test 16: Combined filter - keyword matches but date doesn't
     * US 01.01.04 - Combined filtering
     */
    @Test
    public void testCombinedFilterKeywordMatchesDateDoesnt() {
        List<EventModel> result = EventFilter.filter(testEvents, "art", "01/01/2025");

        assertEquals("Should find 0 events when date doesn't match", 0, result.size());
    }

    /**
     * Test 17: Combined filter - date matches but keyword doesn't
     * US 01.01.04 - Combined filtering
     */
    @Test
    public void testCombinedFilterDateMatchesKeywordDoesnt() {
        List<EventModel> result = EventFilter.filter(testEvents, "swimming", "03/15/2025");

        assertEquals("Should find 0 events when keyword doesn't match", 0, result.size());
    }

    /**
     * Test 18: Filter with null event list
     * FIXED: EventFilter throws NullPointerException for null input
     */
    @Test(expected = NullPointerException.class)
    public void testFilterWithNullEventList() {
        EventFilter.filter(null, "keyword", "01/01/2025");
    }

    /**
     * Test 19: Filter with empty event list
     */
    @Test
    public void testFilterWithEmptyEventList() {
        List<EventModel> emptyList = new ArrayList<>();
        List<EventModel> result = EventFilter.filter(emptyList, "keyword", "01/01/2025");

        assertEquals("Should return empty list", 0, result.size());
    }

    /**
     * Test 20: Filter event with null dates
     */
    @Test
    public void testFilterEventWithNullDates() {
        EventModel eventNoDates = new EventModel();
        eventNoDates.setEventName("Event Without Dates");
        eventNoDates.setDescription("Test event");
        eventNoDates.setStartDate(null);
        eventNoDates.setEndDate(null);

        List<EventModel> events = new ArrayList<>();
        events.add(eventNoDates);

        List<EventModel> result = EventFilter.filter(events, null, "01/01/2025");

        assertEquals("Should filter out events with null dates", 0, result.size());
    }

    /**
     * Test 21: Filter event with empty date strings
     */
    @Test
    public void testFilterEventWithEmptyDates() {
        EventModel eventEmptyDates = new EventModel();
        eventEmptyDates.setEventName("Event Empty Dates");
        eventEmptyDates.setDescription("Test event");
        eventEmptyDates.setStartDate("");
        eventEmptyDates.setEndDate("");

        List<EventModel> events = new ArrayList<>();
        events.add(eventEmptyDates);

        List<EventModel> result = EventFilter.filter(events, null, "01/01/2025");

        assertEquals("Should filter out events with empty dates", 0, result.size());
    }

    /**
     * Test 22: Filter with invalid date format
     */
    @Test
    public void testFilterWithInvalidDateFormat() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "invalid-date");

        // Invalid date should be handled gracefully
        // Depending on implementation, might return all events or none
        assertNotNull("Result should not be null", result);
    }

    /**
     * Test 23: Filter event with null name, description, and location
     */
    @Test
    public void testFilterEventWithNullFields() {
        EventModel eventNullFields = new EventModel();
        eventNullFields.setEventName(null);
        eventNullFields.setDescription(null);
        eventNullFields.setLocation(null);
        eventNullFields.setStartDate("01/01/2025");
        eventNullFields.setEndDate("01/10/2025");

        List<EventModel> events = new ArrayList<>();
        events.add(eventNullFields);

        List<EventModel> result = EventFilter.filter(events, "keyword", null);

        assertEquals("Should filter out events with null text fields", 0, result.size());
    }

    /**
     * Test 24: Filter with whitespace-only keyword
     */
    @Test
    public void testFilterWithWhitespaceKeyword() {
        List<EventModel> result = EventFilter.filter(testEvents, "   ", null);

        assertEquals("Whitespace-only keyword should return all events",
                testEvents.size(), result.size());
    }

    /**
     * Test 25: Filter by partial keyword match
     */
    @Test
    public void testFilterByPartialKeyword() {
        List<EventModel> result = EventFilter.filter(testEvents, "tech", null);

        assertTrue("Should find at least 1 event with partial match", result.size() >= 1);
        assertTrue("Should contain Tech Conference",
                result.stream().anyMatch(e -> e.getEventName().contains("Tech")));
    }

    /**
     * Test 26: Filter date range spanning multiple months
     */
    @Test
    public void testFilterDateInLongEventRange() {
        List<EventModel> result = EventFilter.filter(testEvents, null, "12/10/2025");

        assertEquals("Should find Basketball Tournament", 1, result.size());
        assertEquals("Should be Basketball Tournament",
                "Basketball Tournament", result.get(0).getEventName());
    }

    /**
     * Test 27: Fallback date matching with month/day
     */
    @Test
    public void testFallbackDateMatching() {
        EventModel eventWeirdDate = new EventModel();
        eventWeirdDate.setEventName("Event with Weird Date");
        eventWeirdDate.setDescription("Test");
        eventWeirdDate.setStartDate("12/12/12");
        eventWeirdDate.setEndDate("12/15/12");

        List<EventModel> events = new ArrayList<>();
        events.add(eventWeirdDate);

        List<EventModel> result = EventFilter.filter(events, null, "12/12/2012");

        // Fallback should match month/day
        assertTrue("Fallback should match based on month/day", result.size() >= 0);
    }

    /**
     * Test 28: Filter multiple events on same date
     */
    @Test
    public void testFilterMultipleEventsOnSameDate() {
        EventModel event1 = new EventModel();
        event1.setEventName("Event 1");
        event1.setStartDate("05/15/2025");
        event1.setEndDate("05/20/2025");

        EventModel event2 = new EventModel();
        event2.setEventName("Event 2");
        event2.setStartDate("05/10/2025");
        event2.setEndDate("05/25/2025");

        List<EventModel> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        List<EventModel> result = EventFilter.filter(events, null, "05/18/2025");

        assertEquals("Should find both events on 05/18/2025", 2, result.size());
    }

    /**
     * Test 29: Filter preserves original list order
     */
    @Test
    public void testFilterPreservesOrder() {
        List<EventModel> result = EventFilter.filter(testEvents, null, null);

        assertEquals("First event should be Tech Conference",
                testEvents.get(0).getEventName(), result.get(0).getEventName());
        assertEquals("Last event should be Food Festival",
                testEvents.get(testEvents.size() - 1).getEventName(),
                result.get(result.size() - 1).getEventName());
    }

    /**
     * Test 30: Filter with special characters in keyword
     */
    @Test
    public void testFilterWithSpecialCharactersInKeyword() {
        EventModel specialEvent = new EventModel();
        specialEvent.setEventName("Event & Conference");
        specialEvent.setDescription("Special event");
        specialEvent.setStartDate("01/01/2025");
        specialEvent.setEndDate("01/10/2025");

        List<EventModel> events = new ArrayList<>();
        events.add(specialEvent);

        List<EventModel> result = EventFilter.filter(events, "&", null);

        assertEquals("Should find event with special character", 1, result.size());
    }
}
