package io.rapidpro.flows.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.InteractionTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SubflowTest extends BaseFlowsTest {

    protected Runner m_runner;

    @Before
    public void setupRunState() throws Exception {


        String definitions = readResource("test_flows/subflow.json");
        JsonObject obj = JsonUtils.getGson().fromJson(definitions, JsonObject.class);
        JsonArray flowArray = obj.getAsJsonArray("flows");

        List<Flow> flows = new ArrayList<>();
        for (JsonElement ele : flowArray) {
            flows.add(Flow.fromJson(ele.getAsJsonObject().toString()));
        }

        m_runner = new RunnerBuilder(flows).withLocationResolver(new TestLocationResolver()).build();
    }

    private static class StepMessage {
        String msg;
        Flow flow;

        StepMessage(String msg, Flow flow) {
            this.msg = msg;
            this.flow = flow;
        }
    }

    private List<StepMessage> getMessages(RunState run) {
        List<StepMessage> msgs = new ArrayList<>();
        for (Step step : run.getCompletedSteps()) {
            for (Action action : step.getActions()) {
                if (action instanceof ReplyAction) {
                    msgs.add(new StepMessage(((ReplyAction) action).getMsg().getLocalized(run), step.getFlow()));
                }
            }
        }
        return msgs;
    }

    @Test
    public void testSubflow() throws Exception {

        // start in the parent run
        RunState run = m_runner.start(m_org, m_fields, m_contact, "7c1dee9b-af4c-407b-a269-5553e59149e1");

        // starting the flow should get us our initial prompt
        assertThat(getMessages(run).size(), is(1));
        assertThat(getMessages(run).get(0).msg, is("This is a parent flow. What would you like to do?"));
        assertThat(getMessages(run).get(0).flow.getMetadata().get("name").getAsString(), is("Parent Flow"));

        // respond with "color" which should launch the color subflow
        run = m_runner.resume(run, Input.of("color"));

        // our returned RunState should be for our child flow
        assertThat(getMessages(run).get(0).msg, is("What color do you like?"));
        assertThat(getMessages(run).get(0).flow.getMetadata().get("name").getAsString(), is("Child Flow"));

        // submit in our subflow which should complete it and take us to the parent
        run = m_runner.resume(run, Input.of("red"));

        Step input = run.getSteps().get(0);
        assertThat(input.getRuleResult().getCategory(), is("Red"));
        assertThat(input.getFlow().getMetadata().get("name").getAsString(), is("Child Flow"));

        // we never technically left the last step in our subflow, but it is terminal
        assertTrue(input.isCompleted());
        assertThat(run.getCompletedSteps().size(), is(3));

        assertThat(getMessages(run).get(0).msg, is("Complete: You picked Red."));
        assertThat(getMessages(run).get(1).msg, is("This is a parent flow. What would you like to do?"));

        // run our subflow a second time in the same parent
        run = m_runner.resume(run, Input.of("color"));
        run = m_runner.resume(run, Input.of("green"));
        assertThat(getMessages(run).get(0).msg, is("Complete: You picked Green."));

    }
}
