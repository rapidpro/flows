package io.rapidpro.flows;

import io.rapidpro.expressions.EvaluatorBuilder;
import io.rapidpro.expressions.evaluator.Evaluator;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.Runner;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RunnerBuilder}
 */
public class RunnerBuilderTest extends BaseFlowsTest {

    @Test
    public void build() {

        List<Flow> flows = new ArrayList<Flow>();
        Runner runner = new RunnerBuilder(flows).build();
        assertThat(runner.getTemplateEvaluator(), is(notNullValue()));

        Evaluator evaluator = new EvaluatorBuilder().build();

        Location.Resolver resolver = new Location.Resolver() {
            @Override
            public Location resolve(String input, String country, Location.Level level, Location parent) {
                return null;
            }
        };

        runner = new RunnerBuilder(flows)
                .withTemplateEvaluator(evaluator)
                .withLocationResolver(resolver)
                .build();

        assertThat(runner.getTemplateEvaluator(), is(evaluator));
    }
}
