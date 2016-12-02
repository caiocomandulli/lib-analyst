package com.comandulli.lib.analyst.entity;

import com.comandulli.lib.sqlite.contract.Column;
import com.comandulli.lib.sqlite.contract.Column.DataType;
import com.comandulli.lib.sqlite.contract.Contract;

/**
 * Contract (from Contracts Library) {@see com.comandulli.lib.sqlite.contract.Contract} for the Event Type.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class EventContract extends Contract<Event> {

    /**
     * The constant of the table name.
     */
    public static final String TABLE_NAME = "Event";
    /**
     * The constant of the column id.
     */
    public static final String COLUMN_ID = "Id";
    /**
     * The constant of the column code.
     */
    public static final String COLUMN_CODE = "Code";
    /**
     * The constant of the column data.
     */
    public static final String COLUMN_DATA = "Data";
    /**
     * The constant of the column time.
     */
    public static final String COLUMN_TIME = "Time";
    /**
     * The constant of the column sync.
     */
    public static final String COLUMN_SYNC = "Sync";

    /**
     * Instantiates a new Event contract.
     */
    public EventContract() {
        super(TABLE_NAME);
        columns.add(new Column<Event, Integer>(COLUMN_ID, DataType.INTEGER, true, true) {
            @Override
            public Integer fetchValue(Event obj) {
                return obj.getId();
            }

            @Override
            public void insertValue(Event obj, Integer value) {
                obj.setId(value);
            }
        });
        columns.add(new Column<Event, Integer>(COLUMN_CODE, DataType.INTEGER, false) {
            @Override
            public Integer fetchValue(Event obj) {
                return obj.getType().getCode();
            }

            @Override
            public void insertValue(Event obj, Integer value) {
                obj.setType(new EventType(value));
            }
        });
        columns.add(new Column<Event, String>(COLUMN_DATA, DataType.STRING, false) {
            @Override
            public String fetchValue(Event obj) {
                return obj.getData().toString();
            }

            @Override
            public void insertValue(Event obj, String value) {
                obj.setData(new DataWrapper(value));
            }
        });
        columns.add(new Column<Event, String>(COLUMN_TIME, DataType.STRING, false) {
            @Override
            public String fetchValue(Event obj) {
                return obj.getTimestamp();
            }

            @Override
            public void insertValue(Event obj, String value) {
                obj.setTimestamp(value);
            }
        });
        columns.add(new Column<Event, Integer>(COLUMN_SYNC, DataType.INTEGER, false) {
            @Override
            public Integer fetchValue(Event obj) {
                return obj.isSync() ? 1 : 0;
            }

            @Override
            public void insertValue(Event obj, Integer value) {
                obj.setSync(value > 0);
            }
        });
    }

    /**
     * {@see com.comandulli.lib.sqlite.contract.Contract}
     *
     * @return the event constructed
     */
    @Override
    public Event constructor() {
        return new Event();
    }

}
