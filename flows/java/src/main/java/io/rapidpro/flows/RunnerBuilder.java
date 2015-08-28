package io.rapidpro.flows;

import io.rapidpro.expressions.EvaluatorBuilder;
import io.rapidpro.expressions.evaluator.TemplateEvaluator;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.Runner;

/**
 * Builder for runner instances
 */
public class RunnerBuilder {

    protected TemplateEvaluator m_templateEvaluator;

    protected Location.Resolver m_locationResolver;

    public RunnerBuilder withTemplateEvaluator(TemplateEvaluator templateEvaluator) {
        m_templateEvaluator = templateEvaluator;
        return this;
    }

    public RunnerBuilder withLocationResolver(Location.Resolver locationResolver) {
        m_locationResolver = locationResolver;
        return this;
    }

    public Runner build() {
        if (m_templateEvaluator == null) {
            m_templateEvaluator = new EvaluatorBuilder().build();
        }

        if (m_locationResolver == null) {
            m_locationResolver = new Location.Resolver() {
                @Override
                public Location resolve(String input, String country, Location.Level level, String parent) {
                    return null;
                }
            };
        }

        return new Runner(m_templateEvaluator, m_locationResolver);
    }
}
