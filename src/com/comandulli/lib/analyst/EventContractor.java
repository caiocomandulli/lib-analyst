package com.comandulli.lib.analyst;

import com.comandulli.lib.analyst.entity.Event;
import com.comandulli.lib.analyst.entity.EventContract;
import com.comandulli.lib.sqlite.ContractDatabase;
import com.comandulli.lib.sqlite.contract.Query;
import com.comandulli.lib.sqlite.contract.Query.Selection;

import java.util.List;

/**
 * The type Event contractor.
 * Handles Contract {@see com.comandulli.lib.sqlite.contract.Contract} interactions of the Event {@see com.comandulli.lib.analyst.entity.Event} type.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class EventContractor {

    private final ContractDatabase contractDatabase;

    /**
     * Instantiates a new Event contractor.
     *
     * @param contractDatabase the contract database
     */
    public EventContractor(ContractDatabase contractDatabase) {
        this.contractDatabase = contractDatabase;
    }

    /**
     * Insert a new event.
     *
     * @param event the event
     */
    public void insert(Event event) {
        int id = (int) contractDatabase.insert(event);
        event.setId(id);
    }

    /**
     * Insert a list of events.
     *
     * @param events the events
     */
    public void insertList(List<Event> events) {
        for (Event event : events) {
            insert(event);
        }
    }

    /**
     * Remove event.
     *
     * @param event the event
     */
    public void removeEvent(Event event) {
        Selection selection = new Query().column(EventContract.COLUMN_ID).equalsTo(event.getId()).end();
        contractDatabase.delete(Event.class, selection);
    }

    /**
     * Gets a list of events.
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<Event> getList() {
        return (List<Event>) contractDatabase.select(Event.class);
    }

}
