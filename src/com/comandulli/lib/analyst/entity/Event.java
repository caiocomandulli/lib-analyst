package com.comandulli.lib.analyst.entity;

/**
 * The type Event.
 * This represents an event occuring in your application.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class Event {

    private int id;
    private String timestamp;
    private EventType type;
    private DataWrapper data;
    private boolean sync;

    /**
     * Instantiates a new Event.
     */
    public Event() {
    }

    /**
     * Instantiates a new Event.
     *
     * @param type the type
     * @param time the time
     * @param data the data
     */
    public Event(EventType type, String time, DataWrapper data) {
        this.timestamp = time;
        this.type = type;
        this.data = data;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets timestamp of the creation of this event.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp of the creation of this event.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public EventType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(EventType type) {
        this.type = type;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public DataWrapper getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(DataWrapper data) {
        this.data = data;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "[" + type.getCode() + "]" + type.getName();
    }

    /**
     * Sets if this Event has already been synced.
     *
     * @param sync the sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /**
     * If this event has already been synced.
     *
     * @return the boolean
     */
    public boolean isSync() {
        return sync;
    }

}
