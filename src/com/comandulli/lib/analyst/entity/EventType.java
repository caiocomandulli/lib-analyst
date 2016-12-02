package com.comandulli.lib.analyst.entity;

/**
 * The Event type.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class EventType {

    /**
     * The super types that compose this library.
     */
    public enum SuperType {
        /**
         * When a status is opened.
         */
        Open(1), /**
         * When a status is closed.
         */
        Close(2), /**
         * When a status is suddenly terminated.
         */
        Terminated(3), /**
         * When a status is paused.
         */
        Pause(4), /**
         * When a status is resumed.
         */
        Resume(5);

        private final int identifier;

        SuperType(int identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets identifier.
         *
         * @return the identifier
         */
        public int getIdentifier() {
            return identifier;
        }

    }

    private final int code;
    private final String name;
    private final SuperType superType;

    /**
     * Instantiates a new Event type with only a code.
     *
     * @param code the code
     */
    public EventType(int code) {
        this.code = code;
        this.name = String.valueOf(code);
        char entry = name.charAt(0);
        switch (entry) {
            case '2':
                this.superType = SuperType.Close;
                break;
            case '3':
                this.superType = SuperType.Terminated;
                break;
            case '4':
                this.superType = SuperType.Pause;
                break;
            case '5':
                this.superType = SuperType.Resume;
                break;
            default:
                this.superType = SuperType.Open;
                break;
        }
    }

    /**
     * Instantiates a new Event type with code and super type.
     *
     * @param code      the code
     * @param superType the super type
     */
    public EventType(int code, SuperType superType) {
        this.code = code;
        this.name = String.valueOf(code);
        this.superType = superType;
    }

    /**
     * Instantiates a new complete Event type.
     *
     * @param code      the code
     * @param name      the name
     * @param superType the super type
     */
    public EventType(int code, String name, SuperType superType) {
        this.code = code;
        this.name = name;
        this.superType = superType;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return code + ": " + name;
    }

    /**
     * Gets super type.
     *
     * @return the super type
     */
    public SuperType getSuperType() {
        return superType;
    }

    /**
     * Converts this event to another supert type event.
     *
     * @param newType the new super type
     * @return this type as a new super type.
     */
    public EventType getAsNewType(SuperType newType) {
        String currentCode = String.valueOf(code);
        String newCode = newType.getIdentifier() + currentCode.substring(1);
        return new EventType(Integer.parseInt(newCode));
    }

}
