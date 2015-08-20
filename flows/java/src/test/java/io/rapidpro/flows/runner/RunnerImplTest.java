package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Action;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.TranslatableText;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link RunnerImpl}
 */
public class RunnerImplTest {

    protected Flows.Runner m_runner = new RunnerImpl();

    @Test
    public void startAndResume() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/mushrooms.json"));

        Org org = new Org("eng", ZoneId.of("Africa/Kigali"), true, false);
        Set<String> contactGroups = new HashSet<>(Collections.singleton("Testers"));
        Contact contact = new Contact("1234-1234", "Joe", Arrays.asList(ContactUrn.parse("tel:+260964153686")), contactGroups, new HashMap<String, String>(), "eng");
        Flow flow = Flow.fromJson(flowJson);

        RunState state1 = m_runner.start(org, contact, flow);

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
        assertThat(state1.getSteps(), hasSize(2));
        assertThat(state1.getSteps().get(0).getNode().getUuid(), is("32cf414b-35e3-4c75-8a78-d5f4de925e13"));
        assertThat(state1.getSteps().get(0).getArrivedOn(), notNullValue());
        assertThat(state1.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(state1.getSteps().get(0).getActionResults(), hasSize(1));
        assertReply(state1.getSteps().get(0).getActionResults(), 0, "Hi Joe. Do you like mushrooms?");
        assertThat(state1.getSteps().get(1).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state1.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(state1.getSteps().get(1).getLeftOn(), nullValue());
        assertThat(state1.getSteps().get(1).getRuleResult(), nullValue());

        RunState state2 = m_runner.resume(state1, "YUCK!");

        assertThat(state2.getContact().getGroups(), contains("Testers")); // unchanged
        assertThat(state2.getSteps(), hasSize(3));
        assertThat(state2.getSteps().get(0).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state2.getSteps().get(0).getArrivedOn(), is(state1.getSteps().get(1).getArrivedOn()));
        assertThat(state2.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(state2.getSteps().get(0).getRuleResult().getRule().getUuid(), is("366fb919-7e0b-48be-8f5b-baa14b2a65aa"));
        assertThat(state2.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(state2.getSteps().get(0).getRuleResult().getValue(), is("YUCK!"));
        assertThat(state2.getSteps().get(1).getNode().getUuid(), is("e277932e-d546-4e0c-a483-ce6cce06b929"));
        assertThat(state2.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(state2.getSteps().get(1).getLeftOn(), notNullValue());
        assertThat(state2.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(state2.getSteps().get(1).getActionResults(), hasSize(1));
        assertReply(state2.getSteps().get(1).getActionResults(), 0, "We didn't understand your answer. Please reply with yes/no.");
        assertThat(state2.getSteps().get(2).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state2.getSteps().get(2).getArrivedOn(), notNullValue());
        assertThat(state2.getSteps().get(2).getLeftOn(), nullValue());

        RunState state3 = m_runner.resume(state2, "no");

        assertThat(state3.getContact().getGroups(), containsInAnyOrder("Testers", "Approved")); // added to group
        assertThat(state3.getSteps(), hasSize(2));
        assertThat(state3.getSteps().get(0).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(state3.getSteps().get(0).getArrivedOn(), is(state2.getSteps().get(2).getArrivedOn()));
        assertThat(state3.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(state3.getSteps().get(0).getRuleResult().getRule().getUuid(), is("d638e042-3f5c-4f03-a6c1-2031bd8971b2"));
        assertThat(state3.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(state3.getSteps().get(0).getRuleResult().getValue(), is("no"));
        assertThat(state3.getSteps().get(1).getNode().getUuid(), is("4ef2b232-1484-4db7-b470-98af1a2349d3"));
        assertThat(state3.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(state3.getSteps().get(1).getLeftOn(), nullValue());
        assertThat(state3.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(state3.getSteps().get(1).getActionResults(), hasSize(2));
        assertReply(state3.getSteps().get(1).getActionResults(), 0, "That was the right answer.");
        assertAddToGroup(state3.getSteps().get(1).getActionResults(), 1, "Approved");
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
