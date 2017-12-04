package io.rapidpro.flows.definition.actions.message;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link EmailAction}
 */
public class EmailActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "email",
                "emails", JsonUtils.array("code@nyaruka.com", "@contact.chw_email"),
                "subject", "Salut",
                "msg", "Ça va?");
        EmailAction action = (EmailAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getAddresses(), contains("code@nyaruka.com", "@contact.chw_email"));
        assertThat(action.getSubject(), is("Salut"));
        assertThat(action.getMsg(), is("Ça va?"));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        EmailAction action = new EmailAction(Arrays.asList("rowan@nyaruka.com", "@(LOWER(contact.gender))@chws.org"),
                "Update from @contact", "This is to notify you that @contact did something");

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        EmailAction performed = (EmailAction) result.getPerformed();
        assertThat(performed.getAddresses(), contains("rowan@nyaruka.com", "m@chws.org"));
        assertThat(performed.getSubject(), is("Update from Joe Flow"));
        assertThat(performed.getMsg(), is("This is to notify you that Joe Flow did something"));
    }
}
