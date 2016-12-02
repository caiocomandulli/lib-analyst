package com.comandulli.lib.analyst;

import android.util.Log;

import com.comandulli.analyst.entity.DataWrapper;
import com.comandulli.analyst.entity.Event;
import com.comandulli.analyst.entity.EventType;
import com.comandulli.analyst.entity.EventType.SuperType;
import com.comandulli.lib.TimeStringFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Event logger is a fully static accessible class.
 * <p>
 * It is used to log events to the analyst {@see com.comandulli.lib.analyst.ActionAnalyst}.
 * <p>
 * You can used it throughout your entire application.
 * But first you need to call {@link #init(ActionAnalyst)}
 * to instantiate its initial context.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class EventLogger {

    /**
     * Sets debug mode.
     */
    public static boolean DEBUGMODE;

    private static final List<Event> events = new ArrayList<>();
    private static ActionAnalyst currentAnalyst;
    /**
     * The constant dateFormat for event timestamp formatting.
     */
    public static final SimpleDateFormat dateFormat = new EventDateFormat(TimeStringFormatter.MY_SQL_DATETIME_FORMAT, Locale.UK, TimeStringFormatter.UNIVERSAL_TIME_ZONE);

    /**
     * Init.
     * <p>
     * Initializes the EventLogger context.
     * The analyst is notified by this class whenever an event is logged.
     *
     * @param analyst the analyst
     */
    public static void init(ActionAnalyst analyst) {
        EventContractor contractor = analyst.getContractor();
        List<Event> allEvents = contractor.getList();
        List<Event> sync = new ArrayList<>();
        List<Event> pending = new ArrayList<>();
        for (Event event : allEvents) {
            int eventCode = event.getType().getCode();
            if (eventCode == EVENT_VIEW_RESUME.getCode() || eventCode == EVENT_VIEW_PAUSE.getCode()) {
                events.add(event);
            } else {
                if (event.isSync()) {
                    sync.add(event);
                } else {
                    pending.add(event);
                }
            }
        }
        currentAnalyst = analyst;
        analyst.init(pending, sync);
    }

    /**
     * The constant EVENT_VIEW_RESUME.
     * A view has resumed.
     */
    public static final EventType EVENT_VIEW_RESUME = new EventType(100, "ViewResume", SuperType.Open);
    /**
     * The constant EVENT_VIEW_PAUSE.
     * A view has paused.
     */
    public static final EventType EVENT_VIEW_PAUSE = new EventType(200, "ViewPause", SuperType.Close);

    /**
     * Log a On activity resume.
     *
     * @param activity the activity
     */
    public static void onActivityResume(Class<?> activity) {
        onActivityResume(activity, null, null);
    }

    /**
     * Log a On activity pause.
     *
     * @param activity the activity
     */
    public static void onActivityPause(Class<?> activity) {
        onActivityPause(activity, null, null);
    }

    /**
     * Log a On activity resume.
     *
     * @param activity the activity
     * @param keys     the keys
     * @param values   the values
     */
    public static void onActivityResume(Class<?> activity, String[] keys, Object[] values) {
        onActivity(activity, keys, values, EVENT_VIEW_RESUME);
    }

    /**
     * Log a On activity pause.
     *
     * @param activity the activity
     * @param keys     the keys
     * @param values   the values
     */
    public static void onActivityPause(Class<?> activity, String[] keys, Object[] values) {
        onActivity(activity, keys, values, EVENT_VIEW_PAUSE);
    }

    private static void onActivity(Class<?> activity, String[] keys, Object[] values, EventType type) {
        try {
            HashMap<String, Object> objects = new HashMap<>();
            if (values != null) {
                int valuesLength = values.length;
                if (keys != null && keys.length == valuesLength) {
                    for (int i = 0; i < valuesLength; i++) {
                        objects.put(keys[i], values[i]);
                    }
                }
                objects.put("activity", activity.getSimpleName());
                DataWrapper data = new DataWrapper(objects);
                onEvent(type, data, activity);
            }
        } catch (Exception e) {
            Log.e("ERROR", "Caught error in " + type.getName());
            e.printStackTrace();
            if (DEBUGMODE) {
                throw e;
            }
        }
    }

    /**
     * Log an event.
     *
     * @param eventType the event type
     * @param data      the data
     * @param activity  the activity
     */
    public static void onEvent(EventType eventType, DataWrapper data, Class<?> activity) {
        if (currentAnalyst != null) {
            try {
                String timestamp = dateFormat.format(new Date());
                Event event = new Event(eventType, timestamp, data);
                events.add(event);
                Log.w(activity.getSimpleName(), event.toString());
                currentAnalyst.analyze(event, activity);
            } catch (Exception e) {
                Log.e("ERROR", "Caught error in event logging");
                e.printStackTrace();
                if (DEBUGMODE) {
                    throw e;
                }
            }
        } else {
            Log.i("Analyst", "No Analyst, Event Logger not logging.");
        }
    }

    /**
     * Gets current time.
     *
     * @return the current time
     */
    public static String getCurrentTime() {
        return dateFormat.format(new Date());
    }

    /**
     * Gets the list of events logged.
     *
     * @return the events list
     */
    public static List<Event> getEventsList() {
        return events;
    }

}
