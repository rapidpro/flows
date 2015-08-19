package io.rapidpro.flows;

import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.Contact;
import io.rapidpro.flows.runner.Org;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.RunnerImpl;

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
        RunState newRun(Org org, Contact contact, Flow flow);

        RunState resume(RunState lastState, String input);
    }
}
