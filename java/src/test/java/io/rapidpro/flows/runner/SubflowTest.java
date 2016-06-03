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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
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

    public List<String> getMessages(RunState run) {
        List<String> msgs = new ArrayList<>();
        for (Step step : run.getCompletedSteps()) {
            for (Action action : step.getActions()) {
                if (action instanceof ReplyAction) {
                    String msg = ((ReplyAction) action).getMsg().getLocalized(run);
                    msgs.add(msg);
                }
            }
        }
        return msgs;
    }

    @Test
    public void testSubflow() throws Exception {

        // start in the parent run
        RunState run = m_runner.start(m_org, m_fields, m_contact, 35636);

        // starting the flow should get us our initial prompt
        assertThat(getMessages(run).size(), is(1));
        assertThat(getMessages(run).get(0), is("This is a parent flow. What would you like to do?"));

        // respond with "color" which should launch the color subflow
        run = m_runner.resume(run, Input.of("color"));

        // our returned RunState should be for our child flow
        assertThat(getMessages(run).get(0), is("What color do you like?"));

        // submit in our subflow which should complete it and take us to the parent
        run = m_runner.resume(run, Input.of("red"));
        assertThat(getMessages(run).get(0), is("Complete: You picked Red."));
        assertThat(getMessages(run).get(1), is("This is a parent flow. What would you like to do?"));

        // run our subflow a second time in the same parent
        run = m_runner.resume(run, Input.of("color"));
        run = m_runner.resume(run, Input.of("green"));
        assertThat(getMessages(run).get(0), is("Complete: You picked Green."));

    }

}
