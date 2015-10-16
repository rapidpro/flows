package io.rapidpro.flows.definition.actions.message;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ReplyAction}
 */
public class ReplyActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "reply", "msg", JsonUtils.object("fre", "Bonjour"));
        ReplyAction action = (ReplyAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getMsg(), is(new TranslatableText("fre", "Bonjour")));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        ReplyAction action = new ReplyAction(new TranslatableText("Hi @contact.first_name you said @step.value"));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        ReplyAction performed = (ReplyAction) result.getPerformed();
        assertThat(performed.getMsg(), is(new TranslatableText("Hi Joe you said Yes")));

        // still send if message has errors
        action = new ReplyAction(new TranslatableText("@(badexpression)"));

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));

        performed = (ReplyAction) result.getPerformed();
        assertThat(performed.getMsg(), is(new TranslatableText("@(badexpression)")));
    }
}
