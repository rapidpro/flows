package io.rapidpro.flows.runner;

/**
 * Simple location model
 */
public class Location {

    public enum Level {
        STATE,
        DISTRICT
    }

    protected String m_name;

    public Location(String name) {
        m_name = name;
    }

    /**
     * Interface for anything that can resolve location names
     */
    public interface Resolver {
        /**
         * Resolves a location name from the given input
         * @param input the input to parse
         * @param country the 2-digit country code
         * @param level the level
         * @param parent the parent location name (may be null)
         * @return the location or null if no such location exists
         */
        Location resolve(String input, String country, Level level, String parent);
    }

    public String getName() {
        return m_name;
    }
}
