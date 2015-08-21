package io.rapidpro.flows.runner;

import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.AddToGroupAction;
import io.rapidpro.flows.definition.actions.ReplyAction;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RunnerImpl}
 */
public class RunnerImplTest extends BaseFlowsTest {

    protected Flows.Runner m_runner = new RunnerImpl();

    @Test
    public void mushrooms() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/mushrooms.json"));
        Flow flow = Flow.fromJson(flowJson);

        RunState state1 = m_runner.start(m_org, m_contact, flow);

        assertThat(state1.getOrg().getPrimaryLanguage(), is("eng"));
        assertThat(state1.getOrg().getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(state1.getOrg().isDayFirst(), is(true));
        assertThat(state1.getOrg().isAnon(), is(false));
        assertThat(state1.getContact().getUuid(), is("1234-1234"));
        assertThat(state1.getContact().getName(), is("Joe Flow"));
        assertThat(state1.getContact().getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), new ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")));
        assertThat(state1.getContact().getGroups(), contains("Testers"));
        assertThat(state1.getContact().getFields().size(), is(2));
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
        assertThat(state1.getState(), is(RunState.State.WAIT_MESSAGE));

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
        assertThat(state2.getState(), is(RunState.State.WAIT_MESSAGE));

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
        assertThat(state3.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void mushrooms_french() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/mushrooms.json"));
        Flow flow = Flow.fromJson(flowJson);

        Contact jean = new Contact("1234-1234", "Jean D'Amour", ContactUrn.fromString("tel:+260964153686"), "fre");

        RunState state1 = m_runner.start(m_org, jean, flow);

        assertThat(state1.getContact().getLanguage(), is("fre"));
        assertThat(state1.getSteps(), hasSize(2));
        assertReply(state1.getSteps().get(0).getActionResults(), 0, "Salut Jean. Aimez-vous les champignons?");
        assertThat(state1.getState(), is(RunState.State.WAIT_MESSAGE));

        RunState state2 = m_runner.resume(state1, "EUGH!");

        assertThat(state2.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(state2.getSteps().get(0).getRuleResult().getValue(), is("EUGH!"));
        assertReply(state2.getSteps().get(1).getActionResults(), 0, "Nous ne comprenions pas votre réponse. S'il vous plaît répondre par oui/non.");
        assertThat(state2.getState(), is(RunState.State.WAIT_MESSAGE));

        RunState state3 = m_runner.resume(state2, "non");

        assertThat(state3.getContact().getGroups(), contains("Approved")); // added to group
        assertThat(state3.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(state3.getSteps().get(0).getRuleResult().getValue(), is("non"));
        assertReply(state3.getSteps().get(1).getActionResults(), 0, "Ce fut la bonne réponse.");
        assertThat(state3.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void greatwall() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/greatwall.json"));
        Flow flow = Flow.fromJson(flowJson);

        RunState state1 = m_runner.start(m_org, m_contact, flow);

        assertThat(state1.getSteps().get(0).getNode().getUuid(), is("8dbb7e1a-43d6-4c5b-a99d-fe3ee8923b65"));
        assertThat(state1.getSteps().get(0).getActionResults(), hasSize(1));
        assertReply(state1.getSteps().get(0).getActionResults(), 0, "How many people are you?");
        assertThat(state1.getSteps().get(1).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(state1.getState(), is(RunState.State.WAIT_MESSAGE));

        RunState state2 = m_runner.resume(state1, "9");

        assertThat(state2.getSteps().get(0).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(state2.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(state2.getSteps().get(0).getRuleResult().getValue(), is("9"));
        assertThat(state2.getSteps().get(1).getNode().getUuid(), is("c81af400-a744-499a-9ad5-c90e233e4b92"));
        assertReply(state2.getSteps().get(1).getActionResults(), 0, "Please choose a number between 1 and 8");
        assertThat(state2.getSteps().get(2).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));

        RunState state3 = m_runner.resume(state1, "7");

        assertThat(state3.getSteps().get(0).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(state3.getSteps().get(0).getRuleResult().getCategory(), is("1 - 8"));
        assertThat(state3.getSteps().get(0).getRuleResult().getValue(), is("7"));
    }

    protected void assertReply(List<Action.Result> actions, int index, String msg) {
        ReplyAction action = (ReplyAction) actions.get(index).getAction();
        assertThat(action.getMsg(), is(new TranslatableText(msg)));
    }

    protected void assertAddToGroup(List<Action.Result> actions, int index, String... groups) {
        AddToGroupAction action = (AddToGroupAction) actions.get(index).getAction();
        assertThat(action.getGroups(), contains(groups));
    }
}
