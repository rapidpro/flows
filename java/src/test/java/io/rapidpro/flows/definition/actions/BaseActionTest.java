package io.rapidpro.flows.definition.actions;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;

/**
 * Abstract base class for tests of actions
 */
@Ignore
public abstract class BaseActionTest extends BaseFlowsTest {

    protected Flow.DeserializationContext m_deserializationContext;

    protected Runner m_runner;

    protected RunState m_run;

    protected EvaluationContext m_context;

    @Before
    public void setupRunState() throws Exception {
        String flowJson = IOUtils.toString(BaseActionTest.class.getClassLoader().getResourceAsStream("test_flows/mushrooms.json"));

        Flow flow = Flow.fromJson(flowJson);

        m_deserializationContext = new Flow.DeserializationContext(flow);

        m_runner = new RunnerBuilder().withLocationResolver(new TestLocationResolver()).build();
        m_run = m_runner.start(m_org, m_fields, m_contact, flow);
        m_context = m_run.buildContext(null);
    }
}
