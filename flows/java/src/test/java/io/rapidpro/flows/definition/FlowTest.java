package io.rapidpro.flows.definition;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Flow}
 */
public class FlowTest {

    @org.junit.Test
    public void fromJson() throws IOException {
        String json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("flows/mushrooms.json"));

        Flow definition = Flow.fromJson(json);

        assertThat(definition.getBaseLanguage(), is("eng"));

        ActionSet as1 = (ActionSet) definition.getEntry();

        assertThat(as1.getUuid(), is("32cf414b-35e3-4c75-8a78-d5f4de925e13"));
        assertThat(as1.getActions(), hasSize(1));
        assertThat(as1.getActions().get(0), instanceOf(Action.Reply.class));

        RuleSet rs1 = (RuleSet) as1.getDestination();

        assertThat(rs1.getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(rs1.getType(), is(RuleSet.Type.WAIT_MESSAGE));
        assertThat(rs1.getOperand(), is("@step.value"));

        Rule rs1Rule1 = rs1.getRules().get(0);

        assertThat(rs1Rule1.getTest(), instanceOf(Test.ContainsAny.class));

        ActionSet as2 = (ActionSet) rs1Rule1.getDestination();

        assertThat(as2.getUuid(), is("6d12cde9-dbbf-4673-acd7-afa1776d382b"));
        assertThat(as2.getActions(), hasSize(1));
        assertThat(as2.getActions().get(0), instanceOf(Action.Reply.class));
        assertThat(as2.getDestination(), nullValue());

        Rule rs1Rule2 = rs1.getRules().get(1);

        assertThat(rs1Rule2.getTest(), instanceOf(Test.ContainsAny.class));

        ActionSet as3 = (ActionSet) rs1Rule2.getDestination();

        assertThat(as3.getUuid(), is("4ef2b232-1484-4db7-b470-98af1a2349d3"));
        assertThat(as3.getActions(), hasSize(2));
        assertThat(as3.getActions().get(0), instanceOf(Action.Reply.class));
        assertThat(as3.getActions().get(1), instanceOf(Action.AddToGroup.class));
        assertThat(as3.getDestination(), nullValue());

        Rule rs1Rule3 = rs1.getRules().get(2);

        assertThat(rs1Rule3.getTest(), instanceOf(Test.True.class));

        ActionSet as4 = (ActionSet) rs1Rule3.getDestination();

        assertThat(as4.getUuid(), is("e277932e-d546-4e0c-a483-ce6cce06b929"));
        assertThat(as4.getActions(), hasSize(1));
        assertThat(as4.getActions().get(0), instanceOf(Action.Reply.class));
        assertThat(as4.getDestination(), is((Flow.Node) rs1));
    }
}
