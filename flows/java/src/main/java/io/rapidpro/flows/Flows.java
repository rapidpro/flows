package io.rapidpro.flows;

import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.*;

/**
 * Public interface for the flow engine
 */
public class Flows {
    public static Runner s_runner = new RunnerImpl();

    /**
     * Gets a flow runner instance
     * @return the instance
     */
    public static Runner getRunner() {
        return s_runner;
    }

    /**
     * The flow runner public interface
     */
    public interface Runner {
        /**
         * Starts a new run
         * @param org the org
         * @param contact the contact
         * @param flow the flow
         * @return the run state
         */
        RunState start(Org org, Contact contact, Flow flow) throws FlowRunException;

        /**
         * Resumes an existing run with new input
         * @param lastState the previous run state
         * @param input the new input
         * @return the new run state
         */
        RunState resume(RunState lastState, Input input) throws FlowRunException;
    }
}
