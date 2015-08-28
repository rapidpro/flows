package io.rapidpro.flows.definition;

import io.rapidpro.flows.BaseFlowsTest;
import io.rapidpro.flows.definition.actions.group.AddToGroupsAction;
import io.rapidpro.flows.definition.actions.group.RemoveFromGroupsAction;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.definition.tests.logic.TrueTest;
import io.rapidpro.flows.definition.tests.text.ContainsAnyTest;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Flow}
 */
public class FlowTest extends BaseFlowsTest {

    @Test
    public void fromJson() throws Exception {
        Flow flow = Flow.fromJson(readResource("test_flows/mushrooms.json"));

        assertThat(flow.getBaseLanguage(), is("eng"));

        ActionSet as1 = (ActionSet) flow.getEntry();

        assertThat(as1.getUuid(), is("32cf414b-35e3-4c75-8a78-d5f4de925e13"));
        assertThat(as1.getActions(), hasSize(1));
        assertThat(as1.getActions().get(0), instanceOf(ReplyAction.class));

        RuleSet rs1 = (RuleSet) as1.getDestination();

        assertThat(rs1.getUuid(), is("1e318293-4730-481c-b455-daaaf86b2e6c"));
        assertThat(rs1.getType(), is(RuleSet.Type.WAIT_MESSAGE));
        assertThat(rs1.getLabel(), is("Response 1"));
        assertThat(rs1.getOperand(), is("@step.value"));

        Rule rs1Rule1 = rs1.getRules().get(0);

        assertThat(rs1Rule1.getTest(), instanceOf(ContainsAnyTest.class));
        assertThat(rs1Rule1.getCategory(), is(new TranslatableText("base", "Yes", "eng", "Yes", "fre", "Oui")));

        ActionSet as2 = (ActionSet) rs1Rule1.getDestination();

        assertThat(as2.getUuid(), is("6d12cde9-dbbf-4673-acd7-afa1776d382b"));
        assertThat(as2.getActions(), hasSize(2));
        assertThat(as2.getActions().get(0), instanceOf(ReplyAction.class));
        assertThat(as2.getActions().get(1), instanceOf(RemoveFromGroupsAction.class));
        assertThat(as2.getDestination().getUuid(), is("6891e592-1e29-426b-b227-e3ae466662ab"));

        Rule rs1Rule2 = rs1.getRules().get(1);

        assertThat(rs1Rule2.getTest(), instanceOf(ContainsAnyTest.class));
        assertThat(rs1Rule2.getCategory(), is(new TranslatableText("base", "No", "eng", "No", "fre", "Non")));

        ActionSet as3 = (ActionSet) rs1Rule2.getDestination();

        assertThat(as3.getUuid(), is("4ef2b232-1484-4db7-b470-98af1a2349d3"));
        assertThat(as3.getActions(), hasSize(2));
        assertThat(as3.getActions().get(0), instanceOf(ReplyAction.class));
        assertThat(as3.getActions().get(1), instanceOf(AddToGroupsAction.class));
        assertThat(as3.getDestination().getUuid(), is("6891e592-1e29-426b-b227-e3ae466662ab"));

        Rule rs1Rule3 = rs1.getRules().get(2);

        assertThat(rs1Rule3.getTest(), instanceOf(TrueTest.class));
        assertThat(rs1Rule3.getCategory(), is(new TranslatableText("base", "Other", "eng", "Other", "fre", "Autre")));

        ActionSet as4 = (ActionSet) rs1Rule3.getDestination();

        assertThat(as4.getUuid(), is("e277932e-d546-4e0c-a483-ce6cce06b929"));
        assertThat(as4.getActions(), hasSize(1));
        assertThat(as4.getActions().get(0), instanceOf(ReplyAction.class));
        assertThat(as4.getDestination(), is((Flow.Node) rs1));
    }

    @Test
    public void fromJson_withEmptyFlow() throws Exception {
        Flow flow = Flow.fromJson(readResource("test_flows/empty.json"));

        assertThat(flow.getBaseLanguage(), is("eng"));
        assertThat(flow.getEntry(), nullValue());
    }
}
