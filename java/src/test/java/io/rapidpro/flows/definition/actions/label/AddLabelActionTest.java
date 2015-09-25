package io.rapidpro.flows.definition.actions.label;

import io.rapidpro.flows.definition.LabelRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AddLabelAction}
 */
public class AddLabelActionTest extends BaseActionTest {

    @Test
    public void execute() {
        AddLabelAction action = new AddLabelAction(Arrays.asList(new LabelRef(123, "Testing"), new LabelRef("Messages with @step.value")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        AddLabelAction performed = (AddLabelAction) result.getActionPerformed();

        assertThat(performed.getLabels(), contains(new LabelRef(123, "Testing"), new LabelRef("Messages with Yes")));

        // don't add to group name which is an invalid expression
        action = new AddLabelAction(Arrays.asList(new LabelRef("@(badexpression)")));

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getActionPerformed(), nullValue());
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));
    }
}
