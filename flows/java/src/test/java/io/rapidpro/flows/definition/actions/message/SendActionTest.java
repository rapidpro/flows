package io.rapidpro.flows.definition.actions.message;

import io.rapidpro.flows.definition.ContactRef;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.RecipientVariable;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SendAction}
 */
public class SendActionTest extends BaseActionTest {

    @Test
    public void fromJson() {
        SendAction action = (SendAction) JsonUtils.getGson().fromJson("{" +
                "\"type\":\"send\"," +
                "\"msg\":{" +
                    "\"fre\":\"Bonjour\"" +
                "}," +
                "\"groups\":[" +
                    "{\"id\":123,\"name\":\"Testers\"}" +
                "]," +
                "\"contacts\":[" +
                "{\"id\":234,\"name\":\"Mr Test\"}" +
                "]," +
                "\"variables\":[" +
                "{\"id\":\"@new_contact\"}" +
                "]" +
        "}", Action.class);

        assertThat(action.getMsg(), is(new TranslatableText("fre", "Bonjour")));
        assertThat(action.getGroups().get(0).getId(), is(123));
        assertThat(action.getGroups().get(0).getName(), is("Testers"));
        assertThat(action.getContacts().get(0).getId(), is(234));
        assertThat(action.getContacts().get(0).getName(), is("Mr Test"));
        assertThat(action.getVariables().get(0).getId(), is("@new_contact"));
    }

    @Test
    public void execute() {
        SendAction action = new SendAction(new TranslatableText("Hi @(\"Dr\" & contact) @contact.first_name. @step.contact said @step.value"),
                Arrays.asList(new GroupRef(123, "Testers")),
                Arrays.asList(new ContactRef(234, "Mr Test")),
                Arrays.asList(new RecipientVariable("@new_contact")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        SendAction performed = (SendAction) result.getActionPerformed();

        assertThat(performed.getMsg(), is(new TranslatableText("Hi @(\"Dr\"&contact) @contact.first_name. Joe Flow said Yes")));
    }
}
