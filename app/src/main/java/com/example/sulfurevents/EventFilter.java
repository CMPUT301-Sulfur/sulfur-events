package com.example.sulfurevents;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Filters events for US 01.01.04:
 *  - interests: keyword in name/description/location
 *  - availability: chosen date must be included in the event's date range.
 *
 * Availability is checked in two steps:
 *  1) Try real date parsing/range check (MM/dd/yyyy and MM/dd/yy).
 *  2) If parsing fails or data is weird, fall back to a simple
 *     month/day string match so events that visibly show that date
 *     (e.g., "12/12/12", "12/12/2012") still show up.
 */
public final class EventFilter {

    private static final String TAG = "EventFilter";

    private static final SimpleDateFormat DF_YYYY =
            new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DF_YY =
            new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
    private static final SimpleDateFormat DF_MONTH_DAY =
            new SimpleDateFormat("MM/dd", Locale.getDefault());

    private EventFilter() { }

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

    private static boolean matches(
            EventModel event,
            String keyword,
            Date filterDate,
            String rawDateString
    ) {
        // ---------- interests: keyword search ----------
        boolean matchesKeyword = true;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase(Locale.getDefault());
            String haystack = (safe(event.getEventName())
                    + " " + safe(event.getDescription())
                    + " " + safe(event.getLocation()))
                    .toLowerCase(Locale.getDefault());
            matchesKeyword = haystack.contains(k);
        }

        // ---------- availability: date within range ----------
        boolean matchesDate = true;
        if (filterDate != null) {
            String startStr = safe(event.getStartDate());
            String endStr   = safe(event.getEndDate());

            if (startStr.isEmpty() || endStr.isEmpty()) {
                matchesDate = false;
            } else {
                Date start = parseAnyDate(startStr);
                Date end   = parseAnyDate(endStr);

                if (start != null && end != null) {
                    long t = filterDate.getTime();
                    matchesDate = !filterDate.before(start) && !filterDate.after(end);
                } else {
                    // Fallback: compare just MM/dd as a substring, so things like
                    // "12/12/12" and "12/12/2012" both match a 12/12 filter.
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
     * Try MM/dd/yyyy first, then MM/dd/yy.
     */
    private static Date parseAnyDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String trimmed = s.trim();
        try {
            return DF_YYYY.parse(trimmed);
        } catch (ParseException e1) {
            try {
                return DF_YY.parse(trimmed);
            } catch (ParseException e2) {
                Log.d(TAG, "Failed to parse date: '" + s + "'");
                return null;
            }
        }
    }

    private static String monthDayFrom(Date d) {
        if (d == null) return "";
        return DF_MONTH_DAY.format(d); // e.g., "12/12"
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
