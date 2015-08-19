package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Action;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.TranslatableText;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link RunnerImpl}
 */
public class RunnerImplTest {

    protected Flows.Runner m_runner = new RunnerImpl();

    @Test
    public void run() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("mushrooms.json"));

        Org org = new Org("eng", ZoneId.of("Africa/Kigali"), true, false);
        Contact contact = new Contact("1234-1234", "Joe", Arrays.asList(ContactUrn.parse("tel:+260964153686")), Collections.singleton("Testers"), new HashMap<String, String>(), "eng");
        Flow flow = Flow.fromJson(flowJson);

        RunState state1 = m_runner.newRun(org, contact, flow);

        assertThat(state1.getOrg().getPrimaryLanguage(), is("eng"));
        assertThat(state1.getOrg().getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(state1.getOrg().isDayFirst(), is(true));
        assertThat(state1.getOrg().isAnon(), is(false));
        assertThat(state1.getContact().getUuid(), is("1234-1234"));
        assertThat(state1.getContact().getName(), is("Joe"));
        assertThat(state1.getContact().getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686")));
        assertThat(state1.getContact().getGroups(), contains("Testers"));
        assertThat(state1.getContact().getFields().size(), is(0));
        assertThat(state1.getContact().getLanguage(), is("eng"));
        assertThat(state1.getStepState().getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state1.getActions(), hasSize(1));
        assertReply(state1.getActions(), 0, "Hi Joe. Do you like mushrooms?");

        RunState state2 = m_runner.resume(state1, "YUCK!");

        assertThat(state2.getContact().getGroups(), contains("Testers")); // unchanged
        assertThat(state1.getStepState().getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state2.getActions(), hasSize(1));
        assertReply(state2.getActions(), 0, "We didn't understand your answer. Please reply with yes/no.");

        RunState state3 = m_runner.resume(state2, "No");

        assertThat(state3.getContact().getGroups(), containsInAnyOrder("Testers", "Approved")); // added to group
        assertThat(state1.getStepState().getNode(), nullValue());
        assertThat(state3.getActions(), hasSize(2));
        assertReply(state3.getActions(), 0, "That was the right answer.");
        assertAddToGroup(state3.getActions(), 1, "Approved");

    }

    protected void assertReply(List<Action.Result> actions, int index, String msg) {
        Action.Reply action = (Action.Reply) actions.get(index).getAction();
        assertThat(action.getMsg(), is(new TranslatableText(msg)));
    }

    protected void assertAddToGroup(List<Action.Result> actions, int index, String... groups) {
        Action.AddToGroup action = (Action.AddToGroup) actions.get(index).getAction();
        assertThat(action.getGroups(), contains(groups));
    }
}
