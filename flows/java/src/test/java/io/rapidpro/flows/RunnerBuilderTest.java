package io.rapidpro.flows;

import io.rapidpro.flows.runner.Runner;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RunnerBuilder}
 */
public class RunnerBuilderTest extends BaseFlowsTest {

    @Test
    public void build() {
        Runner runner = new RunnerBuilder().build();
        assertThat(runner, instanceOf(Runner.class));

        runner = new RunnerBuilder().withLocationResolver(null).build();
        assertThat(runner, instanceOf(Runner.class));
    }
}
