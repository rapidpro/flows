package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.io.IOUtils;
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

    protected RunState m_run;

    protected EvaluationContext m_context;

    @Before
    public void setupRunState() throws Exception {
        String flowJson = IOUtils.toString(BaseTestTest.class.getClassLoader().getResourceAsStream("flows/mushrooms.json"));

        Flow flow = Flow.fromJson(flowJson);

        m_deserializationContext = new Flow.DeserializationContext(flow);

        Flows.Runner runner = Flows.getRunner();
        m_run = runner.start(m_org, m_contact, flow);
        m_context = m_run.buildContext(null);
    }

    protected JsonObject parseObject(String json) {
        JsonParser parser = new JsonParser();
        return parser.parse(json).getAsJsonObject();
    }

    protected void assertTest(Test test, String input, boolean expectedMatched, String expectedText) {
        Test.Result result = test.evaluate(m_run, m_context, input);
        assertThat(result.isMatched(), is(expectedMatched));
        assertThat(result.getText(), is(expectedText));
    }
}
