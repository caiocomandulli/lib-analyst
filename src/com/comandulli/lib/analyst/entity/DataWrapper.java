package com.comandulli.lib.analyst.entity;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The type Data wrapper allows you to wrap
 * many values into an internal hash map.
 * <p>
 * It allows you to encode the hash map into a string
 * an then decode again into another Data wrapper.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class DataWrapper {

    private HashMap<String, Object> objects;

    /**
     * Instantiates a new empty Data wrapper.
     */
    public DataWrapper() {
        this.objects = new HashMap<>();
    }

    /**
     * Instantiates a new Data wrapper.
     *
     * @param objects the objects to be wrapped
     */
    public DataWrapper(HashMap<String, Object> objects) {
        this.objects = objects;
    }

    /**
     * Instantiates a new Data wrapper.
     *
     * @param stringValue the encoded data wrapper
     */
    public DataWrapper(String stringValue) {
        if (stringValue != null) {
            objects = new HashMap<>();
            String[] entries = stringValue.split("\\|");
            for (String entry : entries) {
                String[] values = entry.split(":=");
                String key = values[0];
                String value = values[1];
                objects.put(key, value);
            }
        }
    }

    /**
     * Instantiates a new Data wrapper with a single entry.
     *
     * @param key   the key of the entry
     * @param value the value of the entry
     */
    public DataWrapper(String key, String value) {
        this.objects = new HashMap<>();
        this.objects.put(key, value);
    }

    /**
     * Put value.
     *
     * @param key   the key
     * @param value the value
     */
    public void putValue(String key, String value) {
        objects.put(key, value);
    }

    /**
     * Remove value.
     *
     * @param key the key
     */
    public void removeValue(String key) {
        objects.remove(key);
    }

    /**
     * Gets value.
     *
     * @param key the key
     * @return the value
     */
    public String getValue(String key) {
        Object object = objects.get(key);
        if (object == null) {
            return null;
        } else {
            return object.toString();
        }
    }

    /**
     * Encodes this data wrapper into a string.
     *
     * @return the resulting string.
     */
    @Override
    public String toString() {
        String value = "";
        Set<Entry<String, Object>> entrySet = objects.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            value += entry.getKey() + ":=" + entry.getValue() + "|";
        }
        return value.substring(0, value.length() - 1);
    }

}
