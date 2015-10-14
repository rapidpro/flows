package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.junit.Before;
import org.junit.Ignore;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Abstract base class for tests of tests
 */
@Ignore
public abstract class BaseTestTest extends BaseFlowsTest {

    protected Flow.DeserializationContext m_deserializationContext;

    protected Runner m_runner;

    protected RunState m_run;

    protected EvaluationContext m_context;

    @Before
    public void setupRunState() throws Exception {
        Flow flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));

        m_deserializationContext = new Flow.DeserializationContext(flow);

        m_runner = new RunnerBuilder().withLocationResolver(new TestLocationResolver()).build();
        m_run = m_runner.start(m_org, m_fields, m_contact, flow);
        m_context = m_run.buildContext(null);
    }

    protected JsonObject parseObject(String json) {
        JsonParser parser = new JsonParser();
        return parser.parse(json).getAsJsonObject();
    }

    protected void assertTest(Test test, String input, boolean expectedMatched, Object expectedValue) {
        Test.Result result = test.evaluate(m_runner, m_run, m_context, input);
        assertThat(result.isMatched(), is(expectedMatched));
        assertThat(result.getValue(), is(expectedValue));
    }
}
