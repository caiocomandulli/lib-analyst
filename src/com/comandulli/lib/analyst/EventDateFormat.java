package com.comandulli.lib.analyst;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility for formatting event timestamps.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class EventDateFormat extends SimpleDateFormat {

    private static final long serialVersionUID = -525151961887469435L;

    /**
     * Instantiates a new Event date format.
     *
     * @param template the template
     * @param locale   the locale
     * @param timezone the timezone
     */
    public EventDateFormat(String template, Locale locale, String timezone) {
        super(template, locale);
        setTimeZone(TimeZone.getTimeZone(timezone));
    }

}
