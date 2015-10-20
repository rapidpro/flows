package io.rapidpro.flows.definition.actions.group;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AddToGroupsAction}
 */
public class AddToGroupsActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "add_group", "groups", JsonUtils.array(
                JsonUtils.object("id", 123, "name", "Testers"),
                "People who say @step.value"
        ));
        AddToGroupsAction action = (AddToGroupsAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getGroups(), hasSize(2));
        assertThat(action.getGroups().get(0).getId(), is(123));
        assertThat(action.getGroups().get(0).getName(), is("Testers"));
        assertThat(action.getGroups().get(1).getId(), is(nullValue()));
        assertThat(action.getGroups().get(1).getName(), is("People who say @step.value"));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        AddToGroupsAction action = new AddToGroupsAction(Arrays.asList(new GroupRef(123, "Testers"), new GroupRef("People who say @step.value")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        AddToGroupsAction performed = (AddToGroupsAction) result.getPerformed();
        assertThat(performed.getGroups(), contains(new GroupRef(123, "Testers"), new GroupRef("People who say Yes")));

        // don't add to group name which is an invalid expression
        action = new AddToGroupsAction(Arrays.asList(new GroupRef("@(badexpression)")));

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getPerformed(), nullValue());
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));
    }
}
