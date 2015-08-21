package io.rapidpro.flows.definition;

/**
 * Exception thrown when flow JSON is invalid
 */
public class FlowParseException extends Exception {

    public FlowParseException(String message) {
        super(message);
    }
}
