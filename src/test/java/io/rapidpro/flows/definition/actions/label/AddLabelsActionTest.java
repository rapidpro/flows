package io.rapidpro.flows.definition.actions.label;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.LabelRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AddLabelsAction}
 */
public class AddLabelsActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "add_label", "labels", JsonUtils.array(
                JsonUtils.object("id", 123, "name", "Testing"),
                "Messages with @step.value"
        ));
        AddLabelsAction action = (AddLabelsAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getLabels(), hasSize(2));
        assertThat(action.getLabels().get(0).getId(), is(123));
        assertThat(action.getLabels().get(0).getName(), is("Testing"));
        assertThat(action.getLabels().get(1).getId(), is(nullValue()));
        assertThat(action.getLabels().get(1).getName(), is("Messages with @step.value"));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        AddLabelsAction action = new AddLabelsAction(Arrays.asList(new LabelRef(123, "Testing"), new LabelRef("Messages with @step.value")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        AddLabelsAction performed = (AddLabelsAction) result.getPerformed();
        assertThat(performed.getLabels(), contains(new LabelRef(123, "Testing"), new LabelRef("Messages with Yes")));

        // don't add label which is an invalid expression
        action = new AddLabelsAction(Arrays.asList(new LabelRef("@(badexpression)")));

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getPerformed(), nullValue());
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));
    }
}
