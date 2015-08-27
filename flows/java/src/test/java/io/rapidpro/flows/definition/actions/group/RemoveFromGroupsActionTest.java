package io.rapidpro.flows.definition.actions.group;

import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RemoveFromGroupsAction}
 */
public class RemoveFromGroupsActionTest extends BaseActionTest {

    @Test
    public void execute() {
        RemoveFromGroupsAction action = new RemoveFromGroupsAction(Arrays.asList(new GroupRef(123, "Testers"), new GroupRef("People who say @step.value")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        RemoveFromGroupsAction performed = (RemoveFromGroupsAction) result.getActionPerformed();

        assertThat(performed.getGroups(), contains(new GroupRef(123, "Testers"), new GroupRef("People who say Yes")));
    }
}
