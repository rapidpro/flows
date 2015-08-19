package io.rapidpro.flows.definition;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Flow}
 */
public class FlowTest {

    @Test
    public void fromJson() throws IOException {
        String json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("mushrooms.json"));

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
    }
}
