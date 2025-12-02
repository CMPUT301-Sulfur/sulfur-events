package com.example.sulfurevents;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for filtering events based on keyword interests and date availability.
 *
 * Implements filtering for US 01.01.04:
 * - Interests: Searches for keyword matches in event name, description, or location
 * - Availability: Checks if a chosen date falls within the event's date range
 *
 * Date availability checking uses a two-step approach:
 * 1. Attempts proper date parsing (MM/dd/yyyy and MM/dd/yy) and range validation
 * 2. Falls back to simple month/day string matching if parsing fails, ensuring
 *    events with dates like "12/12/12" or "12/12/2012" still match "12/12" filters
 */
public final class EventFilter {

    /** Tag for logging */
    private static final String TAG = "EventFilter";

    /** Date formatter for MM/dd/yyyy format (e.g., "12/25/2024") */
    private static final SimpleDateFormat DF_YYYY =
            new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    /** Date formatter for MM/dd/yy format (e.g., "12/25/24") */
    private static final SimpleDateFormat DF_YY =
            new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

    /** Date formatter for MM/dd format (e.g., "12/25") used for fallback matching */
    private static final SimpleDateFormat DF_MONTH_DAY =
            new SimpleDateFormat("MM/dd", Locale.getDefault());

    /**
     * Filters a list of events based on keyword and date criteria.
     * Both filters are optional - passing null or empty strings will skip that filter.
     *
     * @param allEvents  The complete list of events to filter
     * @param keyword    The keyword to search for in event name, description, or location (case-insensitive)
     * @param dateString The date string to check availability (format: MM/dd/yyyy or MM/dd/yy)
     * @return A filtered list containing only events that match all specified criteria
     */
    public static List<EventModel> filter(
            List<EventModel> allEvents,
            String keyword,
            String dateString
    ) {
        List<EventModel> result = new ArrayList<>();
        Date filterDate = parseAnyDate(dateString);

        for (EventModel e : allEvents) {
            if (matches(e, keyword, filterDate, dateString)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Checks if an event matches the specified keyword and date filters.
     *
     * @param event         The event to check
     * @param keyword       The keyword to search for (null or empty to skip keyword filter)
     * @param filterDate    The parsed date to check availability (null to skip date filter)
     * @param rawDateString The original date string for fallback matching
     * @return true if the event matches all specified filters, false otherwise
     */
    private static boolean matches(
            EventModel event,
            String keyword,
            Date filterDate,
            String rawDateString
    ) {
        // ---------- Interests: keyword search ----------
        boolean matchesKeyword = true;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase(Locale.getDefault());

            // Combine name, description, and location into searchable text
            String haystack = (safe(event.getEventName())
                    + " " + safe(event.getDescription())
                    + " " + safe(event.getLocation()))
                    .toLowerCase(Locale.getDefault());

            matchesKeyword = haystack.contains(k);
        }

        // ---------- Availability: date within range ----------
        boolean matchesDate = true;
        if (filterDate != null) {
            String startStr = safe(event.getStartDate());
            String endStr   = safe(event.getEndDate());

            // Event must have both start and end dates
            if (startStr.isEmpty() || endStr.isEmpty()) {
                matchesDate = false;
            } else {
                Date start = parseAnyDate(startStr);
                Date end   = parseAnyDate(endStr);

                if (start != null && end != null) {
                    // Proper date range check: filterDate must be between start and end (inclusive)
                    long t = filterDate.getTime();
                    matchesDate = !filterDate.before(start) && !filterDate.after(end);
                } else {
                    // Fallback: If date parsing failed, do a simple MM/dd substring match
                    // This ensures dates like "12/12/12" and "12/12/2012" both match "12/12"
                    String monthDay = monthDayFrom(filterDate);
                    matchesDate = startStr.contains(monthDay) || endStr.contains(monthDay);

                    Log.d(TAG, "Fallback date match for '" + event.getEventName()
                            + "': start=" + startStr + ", end=" + endStr
                            + ", filter=" + rawDateString
                            + ", monthDay=" + monthDay
                            + ", matchesDate=" + matchesDate);
                }
            }
        }

        return matchesKeyword && matchesDate;
    }

    /**
     * Attempts to parse a date string using multiple formats.
     * Tries MM/dd/yyyy first, then falls back to MM/dd/yy.
     *
     * @param s The date string to parse
     * @return The parsed Date object, or null if parsing fails
     */
    private static Date parseAnyDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String trimmed = s.trim();

        // Try MM/dd/yyyy format first
        try {
            return DF_YYYY.parse(trimmed);
        } catch (ParseException e1) {
            // Fall back to MM/dd/yy format
            try {
                return DF_YY.parse(trimmed);
            } catch (ParseException e2) {
                Log.d(TAG, "Failed to parse date: '" + s + "'");
                return null;
            }
        }
    }

    /**
     * Extracts the month/day portion from a Date object in MM/dd format.
     * Used for fallback date matching when full date parsing fails.
     *
     * @param d The Date to extract month/day from
     * @return The formatted month/day string (e.g., "12/25"), or empty string if date is null
     */
    private static String monthDayFrom(Date d) {
        if (d == null) return "";
        return DF_MONTH_DAY.format(d); // e.g., "12/12"
    }

    /**
     * Safely handles null strings by returning an empty string.
     *
     * @param s The string to check
     * @return The original string if not null, empty string otherwise
     */
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}