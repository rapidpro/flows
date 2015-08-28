package io.rapidpro.flows;

import io.rapidpro.expressions.EvaluatorBuilder;
import io.rapidpro.expressions.evaluator.TemplateEvaluator;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.Runner;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RunnerBuilder}
 */
public class RunnerBuilderTest extends BaseFlowsTest {

    @Test
    public void build() {
        Runner runner = new RunnerBuilder().build();
        assertThat(runner.getLocationResolver(), is(notNullValue()));
        assertThat(runner.getTemplateEvaluator(), is(notNullValue()));

        TemplateEvaluator evaluator = new EvaluatorBuilder().build();

        Location.Resolver resolver = new Location.Resolver() {
            @Override
            public Location resolve(String input, String country, Location.Level level, String parent) {
                return null;
            }
        };

        runner = new RunnerBuilder()
                .withTemplateEvaluator(evaluator)
                .withLocationResolver(resolver)
                .build();

        assertThat(runner.getTemplateEvaluator(), is(evaluator));
        assertThat(runner.getLocationResolver(), is(resolver));
    }
}
