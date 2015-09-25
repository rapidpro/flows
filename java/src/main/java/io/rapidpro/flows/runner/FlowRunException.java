package io.rapidpro.flows.runner;

/**
 * General exception class for any problems during flow execution
 */
public class FlowRunException extends Exception {

    public FlowRunException(String message) {
        super(message);
    }
}
