package com.comandulli.lib.analyst;

import com.comandulli.lib.analyst.entity.DataWrapper;
import com.comandulli.lib.analyst.entity.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A survey defines how you handle a specific group
 * of event types.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public abstract class ActionSurvey {

    /**
     * The Analyst handling surveys.
     */
    protected final ActionAnalyst analyst;
    /**
     * The Contained events.
     */
    protected final List<String> contained = new ArrayList<>();

    /**
     * Instantiates a new Action survey.
     *
     * @param analyst the analyst
     */
    public ActionSurvey(ActionAnalyst analyst) {
        this.analyst = analyst;
    }

    /**
     * Survey an event, determining how to handle it.
     *
     * @param event    the event
     * @param activity the class
     */
    public void survey(Event event, Class<?> activity) {
        if (event.getType().getCode() == EventLogger.EVENT_VIEW_RESUME.getCode()) {
            surveyResume(event, activity);
        } else if (event.getType().getCode() == EventLogger.EVENT_VIEW_PAUSE.getCode()) {
            surveyPause(event, activity);
        }
    }

    /**
     * Handle a resume.
     *
     * @param event    the event
     * @param activity the class
     */
    public void surveyResume(Event event, Class<?> activity) {

    }

    /**
     * Handle a pause.
     *
     * @param event    the event
     * @param activity the class
     */
    public void surveyPause(Event event, Class<?> activity) {

    }

    /**
     * Handle a Open.
     *
     * @param data the data
     */
    public void open(DataWrapper data) {
        analyst.getDefaultSurvey().open(data);
    }

    /**
     * Handle a Close.
     *
     * @param data the data
     */
    public void close(DataWrapper data) {
        analyst.getDefaultSurvey().close(data);
    }

    /**
     * Handle a Terminate.
     *
     * @param data the data
     */
    public void terminate(DataWrapper data) {
        analyst.getDefaultSurvey().terminate(data);
    }

    /**
     * Handle a Pause.
     *
     * @param data the data
     */
    public void pause(DataWrapper data) {
        analyst.getDefaultSurvey().pause(data);
    }

    /**
     * Handle a Resume.
     *
     * @param data the data
     */
    public void resume(DataWrapper data) {
        analyst.getDefaultSurvey().resume(data);
    }

    /**
     * Add to contained.
     *
     * @param name the name
     */
    public void addToContained(String name) {
        contained.add(name);
    }

    /**
     * If an event is contained.
     *
     * @param name the name
     * @return if contained.
     */
    public boolean isContained(String name) {
        return contained.contains(name);
    }

}
