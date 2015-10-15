package io.rapidpro.flows;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.runner.*;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Flow interaction tests loaded from JSON
 */
public class InteractionTest extends BaseFlowsTest {

    @Test
    public void interactionTests() throws Exception {
        runInteractionTests("test_flows/mushrooms.json", "test_runs/mushrooms.runs.json");
        runInteractionTests("test_flows/registration.json", "test_runs/registration.runs.json");
        runInteractionTests("test_flows/birthdate-check.json", "test_runs/birthdate-check.runs.json");
        runInteractionTests("test_flows/basic-form.json", "test_runs/basic-form.runs.json");
    }

    protected void runInteractionTests(String flowFile, String interactionsFile) throws Exception {
        System.out.println("Running interaction tests from " + interactionsFile);

        Flow flow = Flow.fromJson(readResource(flowFile));

        String interactionsJson = readResource(interactionsFile);
        TestDefinition[] tests = JsonUtils.getGson().fromJson(interactionsJson, TestDefinition[].class);
        Runner runner = new RunnerBuilder()
                .withLocationResolver(new Location.Resolver() {
                    @Override
                    public Location resolve(String input, String country, Location.Level level, Location parent) {
                        // for testing, accept any location that doesn't begin with the letter X
                        if (!input.trim().toLowerCase().startsWith("x")) {
                            return new Location("S0001", input, Location.Level.STATE);
                        } else {
                            return null;
                        }
                    }
                })
                .withNowAs(ZonedDateTime.of(2015, 10, 15, 7, 48, 30, 123456789, ZoneOffset.UTC).toInstant())
                .build();

        for (TestDefinition test : tests) {
            runInteractionTest(runner, flow, test);
        }
    }

    protected void runInteractionTest(Runner runner, Flow flow, TestDefinition test) throws Exception {
        RunState run = null;
        do {
            if (run == null) {
                run = runner.start(test.m_org, test.m_fieldsInitial, test.m_contactInitial, flow);
            } else {
                TestDefinition.Message message = test.m_messages.remove(0);
                assertThat("input", is(message.m_type));

                //System.out.println(" > Resuming run with input: " + message.m_msg);

                runner.resume(run, Input.of(message.m_msg));
            }

            for (Step step : run.getCompletedSteps()) {
                for (Action action : step.getActions()) {
                    if (action instanceof ReplyAction) {
                        String msg = ((ReplyAction) action).getMsg().getLocalized(run);

                        //System.out.println(" > Got reply: " + msg);

                        if (test.m_messages.size() > 0) {
                            TestDefinition.Message message = test.m_messages.remove(0);
                            assertThat("reply", is(message.m_type));
                            assertThat(msg, is(message.m_msg));
                        } else {
                            fail("Got un-expected additional reply: \"" + msg + "\" ");
                        }
                    }
                }
            }

        } while (test.m_messages.size() > 0);

        assertThat(test.m_messages, hasSize(0));

        assertThat(run.getCreatedFields(), is(test.m_fieldsCreated));

        assertThat(run.getContact().getName(), is(test.m_contactFinal.getName()));
        assertThat(run.getContact().getGroups(), is(test.m_contactFinal.getGroups()));
        assertThat(run.getContact().getFields(), is(test.m_contactFinal.getFields()));
        assertThat(run.getContact().getLanguage(), is(test.m_contactFinal.getLanguage()));
    }

    protected static class TestDefinition {
        @SerializedName("org")
        public Org m_org;

        @SerializedName("fields_initial")
        public List<Field> m_fieldsInitial;

        @SerializedName("contact_initial")
        public Contact m_contactInitial;

        @SerializedName("messages")
        public List<Message> m_messages;

        @SerializedName("fields_created")
        public List<Field> m_fieldsCreated;

        @SerializedName("contact_final")
        public Contact m_contactFinal;

        public static class Message {
            @SerializedName("type")
            public String m_type;

            @SerializedName("msg")
            public String m_msg;
        }
    }
}
