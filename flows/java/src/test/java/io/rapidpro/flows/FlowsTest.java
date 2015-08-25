package io.rapidpro.flows;

import io.rapidpro.flows.runner.*;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Flows}
 */
public class FlowsTest extends BaseFlowsTest {

    @Test
    public void runnerBuilder() {
        Flows.Runner runner = new Flows.RunnerBuilder().build();
        assertThat(runner, instanceOf(RunnerImpl.class));

        runner = new Flows.RunnerBuilder().withLocationResolver(null).build();
        assertThat(runner, instanceOf(RunnerImpl.class));
    }
}
