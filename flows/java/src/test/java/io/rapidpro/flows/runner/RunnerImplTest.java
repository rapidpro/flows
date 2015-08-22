package io.rapidpro.flows.runner;

import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.AddToGroupAction;
import io.rapidpro.flows.definition.actions.ReplyAction;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

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

        RunState run = m_runner.start(m_org, m_contact, flow);

        assertThat(run.getOrg().getPrimaryLanguage(), is("eng"));
        assertThat(run.getOrg().getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(run.getOrg().isDayFirst(), is(true));
        assertThat(run.getOrg().isAnon(), is(false));

        assertThat(run.getContact().getUuid(), is("1234-1234"));
        assertThat(run.getContact().getName(), is("Joe Flow"));
        assertThat(run.getContact().getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), new ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")));
        assertThat(run.getContact().getGroups(), contains("Testers"));
        assertThat(run.getContact().getFields().size(), is(2));
        assertThat(run.getContact().getLanguage(), is("eng"));

        assertThat(run.getSteps(), hasSize(2));
        assertThat(run.getSteps().get(0).getNode().getUuid(), is("32cf414b-35e3-4c75-8a78-d5f4de925e13"));
        assertThat(run.getSteps().get(0).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(0).getActionResults(), hasSize(1));
        assertReply(run.getSteps().get(0).getActionResults(), 0, "Hi Joe. Do you like mushrooms?");
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(1).getLeftOn(), nullValue());
        assertThat(run.getSteps().get(1).getRuleResult(), nullValue());

        assertThat(run.getValues().size(), is(0));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        Instant lastStepLeftOn = run.getSteps().get(1).getArrivedOn();

        m_runner.resume(run, "YUCK!");

        assertThat(run.getContact().getGroups(), contains("Testers")); // unchanged

        assertThat(run.getSteps(), hasSize(3));
        assertThat(run.getSteps().get(0).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(0).getArrivedOn(), is(lastStepLeftOn));
        assertThat(run.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(0).getRuleResult().getRule().getUuid(), is("366fb919-7e0b-48be-8f5b-baa14b2a65aa"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("YUCK!"));
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("e277932e-d546-4e0c-a483-ce6cce06b929"));
        assertThat(run.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(1).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(run.getSteps().get(1).getActionResults(), hasSize(1));
        assertReply(run.getSteps().get(1).getActionResults(), 0, "We didn't understand your answer. Please reply with yes/no.");
        assertThat(run.getSteps().get(2).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(2).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(2).getLeftOn(), nullValue());

        assertThat(run.getValues().size(), is(1));
        assertThat(run.getValues().get("response_1").getValue(), is("YUCK!"));
        assertThat(run.getValues().get("response_1").getCategory(), is("Other"));
        assertThat(run.getValues().get("response_1").getText(), is("YUCK!"));
        assertThat(run.getValues().get("response_1").getTime(), notNullValue());

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        lastStepLeftOn = run.getSteps().get(2).getArrivedOn();

        m_runner.resume(run, "no");

        assertThat(run.getContact().getGroups(), containsInAnyOrder("Testers", "Approved")); // added to group

        assertThat(run.getSteps(), hasSize(2));
        assertThat(run.getSteps().get(0).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(0).getArrivedOn(), is(lastStepLeftOn));
        assertThat(run.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(0).getRuleResult().getRule().getUuid(), is("d638e042-3f5c-4f03-a6c1-2031bd8971b2"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("no"));
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("4ef2b232-1484-4db7-b470-98af1a2349d3"));
        assertThat(run.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(1).getLeftOn(), nullValue());
        assertThat(run.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(run.getSteps().get(1).getActionResults(), hasSize(2));
        assertReply(run.getSteps().get(1).getActionResults(), 0, "That was the right answer.");
        assertAddToGroup(run.getSteps().get(1).getActionResults(), 1, "Approved");

        assertThat(run.getValues().size(), is(1));
        assertThat(run.getValues().get("response_1").getValue(), is("no"));
        assertThat(run.getValues().get("response_1").getCategory(), is("No"));
        assertThat(run.getValues().get("response_1").getText(), is("no"));
        assertThat(run.getValues().get("response_1").getTime(), notNullValue());

        assertThat(run.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void mushrooms_french() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/mushrooms.json"));
        Flow flow = Flow.fromJson(flowJson);

        Contact jean = new Contact("1234-1234", "Jean D'Amour", ContactUrn.fromString("tel:+260964153686"), "fre");

        RunState run = m_runner.start(m_org, jean, flow);

        assertThat(run.getContact().getLanguage(), is("fre"));
        assertThat(run.getSteps(), hasSize(2));
        assertReply(run.getSteps().get(0).getActionResults(), 0, "Salut Jean. Aimez-vous les champignons?");
        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, "EUGH!");

        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("EUGH!"));
        assertReply(run.getSteps().get(1).getActionResults(), 0, "Nous ne comprenions pas votre réponse. S'il vous plaît répondre par oui/non.");

        assertThat(run.getValues().get("response_1").getValue(), is("EUGH!"));
        assertThat(run.getValues().get("response_1").getCategory(), is("Other"));
        assertThat(run.getValues().get("response_1").getText(), is("EUGH!"));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, "non");

        assertThat(run.getContact().getGroups(), contains("Approved")); // added to group

        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("non"));
        assertReply(run.getSteps().get(1).getActionResults(), 0, "Ce fut la bonne réponse.");

        assertThat(run.getValues().get("response_1").getValue(), is("non"));
        assertThat(run.getValues().get("response_1").getCategory(), is("No"));
        assertThat(run.getValues().get("response_1").getText(), is("non"));

        assertThat(run.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void greatwall() throws Exception {
        String flowJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/greatwall.json"));
        Flow flow = Flow.fromJson(flowJson);

        RunState run = m_runner.start(m_org, m_contact, flow);

        assertThat(run.getSteps().get(0).getNode().getUuid(), is("8dbb7e1a-43d6-4c5b-a99d-fe3ee8923b65"));
        assertThat(run.getSteps().get(0).getActionResults(), hasSize(1));
        assertReply(run.getSteps().get(0).getActionResults(), 0, "How many people are you?");
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, "9");

        assertThat(run.getSteps().get(0).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("9"));
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("c81af400-a744-499a-9ad5-c90e233e4b92"));
        assertReply(run.getSteps().get(1).getActionResults(), 0, "Please choose a number between 1 and 8");
        assertThat(run.getSteps().get(2).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));

        assertThat(run.getValues().get("people").getValue(), is("9"));
        assertThat(run.getValues().get("people").getCategory(), is("Other"));
        assertThat(run.getValues().get("people").getText(), is("9"));

        m_runner.resume(run, "7");

        assertThat(run.getSteps().get(0).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("1 - 8"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("7"));

        assertThat(run.getSteps().get(1).getNode().getUuid(), is("fe5ec555-ed5b-4b29-934d-c593f52c5881"));
        assertThat(run.getSteps().get(1).getRuleResult().getCategory(), is("> 2"));
        assertThat(run.getSteps().get(1).getRuleResult().getValue(), is("7"));

        assertThat(run.getValues().get("people").getValue(), is("7"));
        assertThat(run.getValues().get("people").getCategory(), is("1 - 8"));
        assertThat(run.getValues().get("people").getText(), is("7"));
        assertThat(run.getValues().get("enough_for_soup").getValue(), is("7"));
        assertThat(run.getValues().get("enough_for_soup").getCategory(), is("> 2"));
        assertThat(run.getValues().get("enough_for_soup").getText(), is("7"));
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
