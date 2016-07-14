package io.rapidpro.flows;

import io.rapidpro.expressions.EvaluatorBuilder;
import io.rapidpro.expressions.evaluator.Evaluator;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.Runner;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for runner instances
 */
public class RunnerBuilder {

    protected Evaluator m_templateEvaluator;

    protected Location.Resolver m_locationResolver;

    protected Instant m_now;

    protected List<Flow> m_flows;

    public RunnerBuilder(List<Flow> flows) {
        m_flows = flows;
    }

    public RunnerBuilder() {
        m_flows = new ArrayList<>();
    }

    public RunnerBuilder withTemplateEvaluator(Evaluator templateEvaluator) {
        m_templateEvaluator = templateEvaluator;
        return this;
    }

    public RunnerBuilder withLocationResolver(Location.Resolver locationResolver) {
        m_locationResolver = locationResolver;
        return this;
    }

    public RunnerBuilder withNowAs(Instant now) {
        m_now = now;
        return this;
    }

    public Runner build() {
        if (m_templateEvaluator == null) {
            m_templateEvaluator = new EvaluatorBuilder()
                    .withExpressionPrefix('@')
                    .withAllowedTopLevels(new String[]{"channel", "contact", "date", "extra", "flow", "step", "parent", "child"})
                    .build();
        }

        if (m_locationResolver == null) {
            m_locationResolver = new Location.Resolver() {
                @Override
                public Location resolve(String input, String country, Location.Level level, Location parent) {
                    return null;
                }
            };
        }

        return new Runner(m_templateEvaluator, m_locationResolver, m_now, m_flows);
    }
}
