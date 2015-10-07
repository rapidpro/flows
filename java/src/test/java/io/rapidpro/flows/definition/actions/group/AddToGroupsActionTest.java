package io.rapidpro.flows.definition.actions.group;

import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AddToGroupsAction}
 */
public class AddToGroupsActionTest extends BaseActionTest {

    @Test
    public void execute() {
        AddToGroupsAction action = new AddToGroupsAction(Arrays.asList(new GroupRef(123, "Testers"), new GroupRef("People who say @step.value")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        AddToGroupsAction performed = (AddToGroupsAction) result.getPerformed();

        assertThat(performed.getGroups(), contains(new GroupRef(123, "Testers"), new GroupRef("People who say Yes")));

        // don't add to group name which is an invalid expression
        action = new AddToGroupsAction(Arrays.asList(new GroupRef("@(badexpression)")));

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getPerformed(), nullValue());
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));
    }
}
