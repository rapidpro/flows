package io.rapidpro.flows;

import io.rapidpro.flows.runner.RunnerImpl;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Flows}
 */
public class FlowsTest {

    @Test
    public void getRunner() {
        Flows.Runner runner = new Flows.RunnerBuilder().build();

        assertThat(runner, instanceOf(RunnerImpl.class));

        runner = new Flows.RunnerBuilder().withLocationResolver(null).build();

        assertThat(runner, instanceOf(RunnerImpl.class));
    }
}
