package io.rapidpro.flows;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.dates.DateParser;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.runner.*;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 */
public class FlowsTest extends BaseFlowsTest {

    @Test
    public void interactionTests() throws Exception {
        runInteractionTests("test_flows/mushrooms.json", "test_runs/mushrooms_runs.json");
        runInteractionTests("test_flows/registration.json", "test_runs/registration_runs.json");
    }

    protected void runInteractionTests(String flowFile, String interactionsFile) throws Exception {
        Flow flow = Flow.fromJson(readResource(flowFile));

        String interactionsJson = readResource(interactionsFile);
        InteractionTest[] tests = JsonUtils.getGson().fromJson(interactionsJson, InteractionTest[].class);
        Runner runner = new RunnerBuilder()
                .withLocationResolver(new Location.Resolver() {
                    @Override
                    public Location resolve(String input, String country, Location.Level level, String parent) {
                        // for testing, accept any location that doesn't begin with the letter X
                        if (!input.trim().toLowerCase().startsWith("x")) {
                            return new Location("S0001", input, Location.Level.STATE);
                        } else {
                            return null;
                        }
                    }
                })
                .build();

        for (InteractionTest test : tests) {
            runInteractionTest(runner, flow, test);
        }
    }

    protected void runInteractionTest(Runner runner, Flow flow, InteractionTest test) throws Exception {
        RunState run = null;
        do {
            if (run == null) {
                run = runner.start(test.m_org, test.m_contactInitial, flow);
            } else {
                InteractionTest.Message message = test.m_messages.remove(0);
                assertThat("input", is(message.m_type));

                runner.resume(run, Input.of(message.m_msg));
            }

            for (Step step : run.getCompletedSteps()) {
                for (Action action : step.getActions()) {
                    if (action instanceof ReplyAction) {
                        String msg = ((ReplyAction) action).getMsg().getLocalized(run);

                        if (test.m_messages.size() > 0) {
                            InteractionTest.Message message = test.m_messages.remove(0);
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

        assertThat(run.getContact().getName(), is(test.m_contactFinal.getName()));
        assertThat(run.getContact().getGroups(), is(test.m_contactFinal.getGroups()));
        assertFields(run.getOrg(), run.getContact().getFields(), test.m_contactFinal.getFields());
        assertThat(run.getContact().getLanguage(), is(test.m_contactFinal.getLanguage()));
    }

    /**
     * Fields can contain dynamic datetime values. If an expected field has [[NOW]], we compare it to the current
     * time with a +/- 1 minute error margin
     */
    protected void assertFields(Org org, Map<String, String> actual, Map<String, String> expected) {
        assertThat(actual.keySet(), is(expected.keySet()));

        for (Map.Entry<String, String> entry : actual.entrySet()) {
            String actualValue = entry.getValue();
            String expectedValue = expected.get(entry.getKey());

            if (expectedValue.equals("[[NOW]]")) {
                DateParser dateParser = new DateParser(LocalDate.now(), org.getTimezone(), org.getDateStyle());
                ZonedDateTime actualDateTime = (ZonedDateTime) dateParser.auto(actualValue);
                Instant actualInstant = Instant.from(actualDateTime);

                // equaliy check with +/- 1 minute error margin
                assertThat(actualInstant, is(greaterThan(Instant.now().minus(60, ChronoUnit.SECONDS))));
                assertThat(actualInstant, is(lessThan(Instant.now().plus(60, ChronoUnit.SECONDS))));

            } else {
                assertThat(actualValue, is(expectedValue));
            }
        }
    }

    protected static class InteractionTest {
        @SerializedName("org")
        public Org m_org;

        @SerializedName("contact_initial")
        public Contact m_contactInitial;

        @SerializedName("messages")
        public List<Message> m_messages;

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
