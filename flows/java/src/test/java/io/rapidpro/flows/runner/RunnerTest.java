package io.rapidpro.flows.runner;

import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.RunnerBuilder;
import io.rapidpro.flows.definition.Flow;
import org.junit.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Runner}
 */
public class RunnerTest extends BaseFlowsTest {

    protected Runner m_runner = new RunnerBuilder().build();

    @Test
    public void startAndResume_mushrooms() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/mushrooms.json"));

        RunState run = m_runner.start(getOrg(), getContact(), flow);

        assertThat(run.getOrg().getPrimaryLanguage(), is("eng"));
        assertThat(run.getOrg().getTimezone(), is(ZoneId.of("Africa/Kigali")));
        assertThat(run.getOrg().getDateStyle(), is(DateStyle.DAY_FIRST));
        assertThat(run.getOrg().isAnon(), is(false));

        assertThat(run.getContact().getUuid(), is("1234-1234"));
        assertThat(run.getContact().getName(), is("Joe Flow"));
        assertThat(run.getContact().getUrns(), contains(new ContactUrn(ContactUrn.Scheme.TEL, "+260964153686"), new ContactUrn(ContactUrn.Scheme.TWITTER, "realJoeFlow")));
        assertThat(run.getContact().getGroups(), containsInAnyOrder("Testers", "Developers"));
        assertThat(run.getContact().getFields().size(), is(2));
        assertThat(run.getContact().getLanguage(), is("eng"));

        assertThat(run.getSteps(), hasSize(2));
        assertThat(run.getSteps().get(0).getNode().getUuid(), is("32cf414b-35e3-4c75-8a78-d5f4de925e13"));
        assertThat(run.getSteps().get(0).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(0).getActions(), hasSize(1));
        assertReply(run.getSteps().get(0).getActions().get(0), "Hi Joe. Do you like mushrooms?");
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(1).getLeftOn(), nullValue());
        assertThat(run.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(run.getCompletedSteps(), hasSize(1));

        assertThat(run.getValues().size(), is(0));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        Instant lastStepLeftOn = run.getSteps().get(1).getArrivedOn();

        m_runner.resume(run, Input.of("YUCK!"));

        assertThat(run.getContact().getGroups(), containsInAnyOrder("Testers", "Developers")); // unchanged

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
        assertThat(run.getSteps().get(1).getActions(), hasSize(1));
        assertReply(run.getSteps().get(1).getActions().get(0), "We didn't understand your answer. Please reply with yes/no.");
        assertThat(run.getSteps().get(2).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(2).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(2).getLeftOn(), nullValue());
        assertThat(run.getCompletedSteps(), hasSize(2));

        assertThat(run.getValues().size(), is(1));
        assertThat(run.getValues().get("response_1").getValue(), is("YUCK!"));
        assertThat(run.getValues().get("response_1").getCategory(), is("Other"));
        assertThat(run.getValues().get("response_1").getText(), is("YUCK!"));
        assertThat(run.getValues().get("response_1").getTime(), notNullValue());

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        lastStepLeftOn = run.getSteps().get(2).getArrivedOn();

        m_runner.resume(run, Input.of("no way"));

        assertThat(run.getContact().getGroups(), containsInAnyOrder("Testers", "Developers", "Approved")); // added to group

        assertThat(run.getSteps(), hasSize(3));
        assertThat(run.getSteps().get(0).getNode().getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(run.getSteps().get(0).getArrivedOn(), is(lastStepLeftOn));
        assertThat(run.getSteps().get(0).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(0).getRuleResult().getRule().getUuid(), is("d638e042-3f5c-4f03-a6c1-2031bd8971b2"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("no"));
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("4ef2b232-1484-4db7-b470-98af1a2349d3"));
        assertThat(run.getSteps().get(1).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(1).getLeftOn(), notNullValue());
        assertThat(run.getSteps().get(1).getRuleResult(), nullValue());
        assertThat(run.getSteps().get(1).getActions(), hasSize(2));
        assertReply(run.getSteps().get(1).getActions().get(0), "That was the right answer.");
        assertAddToGroup(run.getSteps().get(1).getActions().get(1), "Approved");
        assertThat(run.getSteps().get(2).getNode().getUuid(), is("dc495df8-8a4d-4cec-951a-56b321a0c828"));
        assertThat(run.getSteps().get(2).getArrivedOn(), notNullValue());
        assertThat(run.getSteps().get(2).getLeftOn(), nullValue());
        assertThat(run.getSteps().get(2).getRuleResult(), nullValue());
        assertThat(run.getSteps().get(2).getActions(), hasSize(1));
        assertThat(run.getCompletedSteps(), hasSize(2));

        assertThat(run.getValues().size(), is(1));
        assertThat(run.getValues().get("response_1").getValue(), is("no"));
        assertThat(run.getValues().get("response_1").getCategory(), is("No"));
        assertThat(run.getValues().get("response_1").getText(), is("no way"));
        assertThat(run.getValues().get("response_1").getTime(), notNullValue());

        assertThat(run.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void startAndResume_mushroomsInFrench() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/mushrooms.json"));

        Contact jean = new Contact("1234-1234", "Jean D'Amour", ContactUrn.fromString("tel:+260964153686"), "fre");

        RunState run = m_runner.start(getOrg(), jean, flow);

        assertThat(run.getContact().getLanguage(), is("fre"));
        assertThat(run.getSteps(), hasSize(2));
        assertReply(run.getSteps().get(0).getActions().get(0), "Salut Jean. Aimez-vous les champignons?");
        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, Input.of("EUGH!"));

        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("EUGH!"));
        assertReply(run.getSteps().get(1).getActions().get(0), "Nous ne comprenions pas votre réponse. S'il vous plaît répondre par oui/non.");

        assertThat(run.getValues().get("response_1").getValue(), is("EUGH!"));
        assertThat(run.getValues().get("response_1").getCategory(), is("Other"));
        assertThat(run.getValues().get("response_1").getText(), is("EUGH!"));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, Input.of("non"));

        assertThat(run.getContact().getGroups(), contains("Approved")); // added to group

        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("No"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("non"));
        assertReply(run.getSteps().get(1).getActions().get(0), "Ce fut la bonne réponse.");

        assertThat(run.getValues().get("response_1").getValue(), is("non"));
        assertThat(run.getValues().get("response_1").getCategory(), is("No"));
        assertThat(run.getValues().get("response_1").getText(), is("non"));

        assertThat(run.getState(), is(RunState.State.COMPLETED));
    }

    @Test
    public void startAndResume_greatwall() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/greatwall.json"));

        RunState run = m_runner.start(getOrg(), getContact(), flow);

        assertThat(run.getSteps().get(0).getNode().getUuid(), is("8dbb7e1a-43d6-4c5b-a99d-fe3ee8923b65"));
        assertThat(run.getSteps().get(0).getActions(), hasSize(1));
        assertReply(run.getSteps().get(0).getActions().get(0), "How many people are you?");
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));

        assertThat(run.getState(), is(RunState.State.WAIT_MESSAGE));

        m_runner.resume(run, Input.of("9"));

        assertThat(run.getSteps().get(0).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));
        assertThat(run.getSteps().get(0).getRuleResult().getCategory(), is("Other"));
        assertThat(run.getSteps().get(0).getRuleResult().getValue(), is("9"));
        assertThat(run.getSteps().get(1).getNode().getUuid(), is("c81af400-a744-499a-9ad5-c90e233e4b92"));
        assertReply(run.getSteps().get(1).getActions().get(0), "Please choose a number between 1 and 8");
        assertThat(run.getSteps().get(2).getNode().getUuid(), is("b7cfa0ac-4d50-4384-a1ab-9ec79bd45e42"));

        assertThat(run.getValues().get("people").getValue(), is("9"));
        assertThat(run.getValues().get("people").getCategory(), is("Other"));
        assertThat(run.getValues().get("people").getText(), is("9"));

        m_runner.resume(run, Input.of("7"));

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

    @Test(expected = FlowRunException.class)
    public void start_emptyFlow() throws Exception {
        Flow flow = Flow.fromJson(readResource("flows/empty.json"));
        m_runner.start(getOrg(), getContact(), flow);
    }
}
