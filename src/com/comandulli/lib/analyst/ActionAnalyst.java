package com.comandulli.lib.analyst;

import android.content.Context;
import android.util.Log;

import com.comandulli.lib.analyst.entity.Event;
import com.comandulli.lib.sqlite.ContractDatabase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * The Analyst is responsible for handling all logs incoming.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class ActionAnalyst {

    /**
     * The current analyst.
     */
    public static ActionAnalyst currentAnalyst;

    private List<Event> pending = new ArrayList<>();
    private List<Event> toSync = new ArrayList<>();
    private final Hashtable<String, ActionSurvey> availableSurveys = new Hashtable<>();
    private ActionSurvey defaultSurvey;
    /**
     * The current Contractor in use.
     */
    protected final EventContractor contractor;
    /**
     * If it is surveying.
     */
    protected boolean surveying;
    private final Context context;

    /**
     * Instantiates a new analyst.
     *
     * @param context          the android context
     * @param contractDatabase the contract database
     */
    public ActionAnalyst(Context context, ContractDatabase contractDatabase) {
        currentAnalyst = this;
        this.context = context;
        this.contractor = new EventContractor(contractDatabase);
    }

    /**
     * Method for the event logger to initialize this analyst.
     *
     * @param pending the pending
     * @param toSync  to sync
     */
    public void init(List<Event> pending, List<Event> toSync) {
        this.pending = pending;
        this.toSync = toSync;
    }

    /**
     * Analyze an event.
     * <p>
     * It starts a new thread to handle an incoming event.
     * Only one analyze thread is able to run at a time.
     * All other analyze calls are queued.
     * <p>
     * On analysis it sends the event to the proper survey.
     *
     * @param event    the event
     * @param activity the class
     */
    public void analyze(final Event event, final Class<?> activity) {
        // analyze new event with latest ones and determine the type of it
        Runnable surveyThread = new Runnable() {
            @Override
            public void run() {
                boolean continueRunning = true;
                while (continueRunning) {
                    try {
                        if (!surveying) {
                            continueRunning = false;
                            surveying = true;
                            ActionSurvey survey = getSurvey(activity);
                            survey.survey(event, activity);
                            surveying = false;
                        }
                    } catch (Exception e) {
                        Log.e("ERROR", "Caught error in analyze");
                        e.printStackTrace();
                        if (EventLogger.DEBUGMODE) {
                            throw e;
                        }
                    }
                }
            }
        };
        new Thread(surveyThread).start();
    }

    /**
     * Sets default survey for all events.
     *
     * @param defaultSurvey the default survey
     */
    public void setDefaultSurvey(ActionSurvey defaultSurvey) {
        this.defaultSurvey = defaultSurvey;
    }

    /**
     * Add a survey to handle a group of event types.
     *
     * @param activity the class
     * @param survey   the survey
     */
    public void addSurvey(Class<?> activity, ActionSurvey survey) {
        availableSurveys.put(activity.getSimpleName(), survey);
    }

    /**
     * Gets default survey.
     *
     * @return the default survey
     */
    public ActionSurvey getDefaultSurvey() {
        return defaultSurvey;
    }

    /**
     * Gets current contractor.
     *
     * @return the contractor
     */
    public EventContractor getContractor() {
        return contractor;
    }

    /**
     * Gets survey for a class type.
     *
     * @param activity the class
     * @return the survey
     */
    public ActionSurvey getSurvey(Class<?> activity) {
        ActionSurvey survey = availableSurveys.get(activity.getSimpleName());
        if (survey == null) {
            survey = defaultSurvey;
        }
        return survey;
    }

    /**
     * Gets survey by the Activity name (Activity.getSimpleName()).
     *
     * @param name the name
     * @return the survey
     */
    public ActionSurvey getSurvey(String name) {
        ActionSurvey survey = availableSurveys.get(name);
        if (survey == null) {
            survey = defaultSurvey;
        }
        return survey;
    }

    /**
     * Add an event as pending
     *
     * @param event the event
     */
    public void addToPending(Event event) {
        event.setSync(false);
        pending.add(event);
        contractor.insert(event);
        Log.w("ACT-PENDING:" + event.getType().getName(), event.toString());
    }

    /**
     * Add to sync.
     *
     * @param event the event
     */
    public void addToSync(Event event) {
        event.setSync(true);
        toSync.add(event);
        contractor.insert(event);
        Log.w("ACT-SYNC:" + event.getType().getName(), event.toString());
    }

    /**
     * Move from pending to sync.
     *
     * @param event the event
     */
    public void moveFromPendingToSync(Event event) {
        removeFromPending(event);
        addToSync(event);
    }

    /**
     * Remove from pending.
     *
     * @param event the event
     */
    public void removeFromPending(Event event) {
        pending.remove(event);
        contractor.removeEvent(event);
        Log.w("ACT-UNPEND:" + event.getType().getName(), event.toString());
    }

    /**
     * Remove from sync.
     *
     * @param event the event
     */
    public void removeFromSync(Event event) {
        toSync.remove(event);
        contractor.removeEvent(event);
        Log.w("ACT-SYNCED:" + event.getType().getName(), event.toString());
    }

    /**
     * Search pending event.
     *
     * @param code the code of the event type
     * @return the event
     */
    public Event searchPendingEvent(int code) {
        for (Event event : pending) {
            if (event.getType().getCode() == code) {
                return event;
            }
        }
        return null;
    }

    /**
     * Gets last pending event.
     *
     * @return the last pending event
     */
    public Event getLastPendingEvent() {
        if (!pending.isEmpty()) {
            return pending.get(pending.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * Gets total number of events requiring syncing.
     *
     * @return the number
     */
    public int getSyncSize() {
        return toSync.size();
    }

    /**
     * Gets total number of pending events.
     *
     * @return the number
     */
    public int getPendingSize() {
        return pending.size();
    }

    /**
     * Get pending events as an array.
     *
     * @return the event array
     */
    public Event[] getPendingAsArray() {
        Event[] pendingArray = new Event[getPendingSize()];
        pending.toArray(pendingArray);
        return pendingArray;
    }

    /**
     * Get events that still need syncing as an array.
     *
     * @return the event array
     */
    public Event[] getToSyncAsArray() {
        Event[] syncArray = new Event[getSyncSize()];
        toSync.toArray(syncArray);
        return syncArray;
    }

    /**
     * Gets android context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

}
