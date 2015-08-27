package io.rapidpro.flows;

import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.Runner;

/**
 * Builder for runner instances
 */
public class RunnerBuilder {

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
        return new Runner(m_locationResolver);
    }
}
