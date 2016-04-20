package io.rapidpro.flows.runner;

/**
 * Simple location model
 */
public class Location {

    public enum Level {
        STATE,
        DISTRICT,
        WARD
    }

    protected String m_osmId;

    protected String m_name;

    protected Level m_level;

    public Location(String osmId, String name, Level level) {
        m_osmId = osmId;
        m_name = name;
        m_level = level;
    }

    /**
     * Interface for anything that can resolve location names
     */
    public interface Resolver {
        /**
         * Resolves a location name from the given input
         * @param text the text to parse
         * @param country the 2-digit country code
         * @param level the level
         * @param parent the parent location (may be null)
         * @return the location or null if no such location exists
         */
        Location resolve(String text, String country, Level level, Location parent);
    }

    public String getOsmId() {
        return m_osmId;
    }

    public String getName() {
        return m_name;
    }

    public Level getLevel() {
        return m_level;
    }
}
