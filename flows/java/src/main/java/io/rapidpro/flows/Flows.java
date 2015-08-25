package io.rapidpro.flows;

import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.*;

/**
 * Public interface for the flow engine
 */
public class Flows {

    /**
     * Builder for runner instances
     */
    public static class RunnerBuilder {
        protected Location.Resolver m_locationResolver = new Location.Resolver() {
            @Override
            public Location resolve(String input, String country, Location.Level level, String parent) {
                return null;
            }
        };

        public RunnerBuilder withLocationResolver(Location.Resolver locationResolver) {
            m_locationResolver = locationResolver;
            return this;
        }

        public Runner build() {
            return new RunnerImpl(m_locationResolver);
        }
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

        /**
         * Gets the location resolver used by this runner
         * @return the resolver
         */
        Location.Resolver getLocationResolver();
    }
}
