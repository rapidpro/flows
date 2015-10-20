package io.rapidpro.flows.definition.actions.message;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.ContactRef;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.VariableRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SendAction}
 */
public class SendActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object(
                "type", "send",
                "contacts", JsonUtils.array(JsonUtils.object("id", 123, "name", "Mr Test")),
                "groups", JsonUtils.array(JsonUtils.object("id", 234, "name", "Testers")),
                "variables", JsonUtils.array(
                        JsonUtils.object("id", "@new_contact"),
                        JsonUtils.object("id", "group-@contact.gender")
                ),
                "msg", JsonUtils.object("fre", "Bonjour")
        );
        SendAction action = (SendAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getMsg(), is(new TranslatableText("fre", "Bonjour")));
        assertThat(action.getContacts().get(0).getId(), is(123));
        assertThat(action.getContacts().get(0).getName(), is("Mr Test"));
        assertThat(action.getGroups().get(0).getId(), is(234));
        assertThat(action.getGroups().get(0).getName(), is("Testers"));
        assertThat(action.getVariables().get(0).getValue(), is("@new_contact"));
        assertThat(action.getVariables().get(1).getValue(), is("group-@contact.gender"));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        SendAction action = new SendAction(new TranslatableText("Hi @(\"Dr\" & contact) @contact.first_name. @step.contact said @step.value"),
                Arrays.asList(new ContactRef(234, "Mr Test")),
                Arrays.asList(new GroupRef(123, "Testers")),
                Arrays.asList(new VariableRef("@new_contact"), new VariableRef("group-@contact.gender")));

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        SendAction performed = (SendAction) result.getPerformed();
        assertThat(performed.getMsg(), is(new TranslatableText("Hi @(\"Dr\"&contact) @contact.first_name. Joe Flow said Yes")));
        assertThat(performed.getGroups().size(), is(1));
        assertThat(performed.getGroups().get(0).getId(), is(123));
        assertThat(performed.getGroups().get(0).getName(), is("Testers"));
        assertThat(performed.getContacts().size(), is(1));
        assertThat(performed.getContacts().get(0).getId(), is(234));
        assertThat(performed.getContacts().get(0).getName(), is("Mr Test"));
        assertThat(performed.getVariables().size(), is(2));
        assertThat(performed.getVariables().get(0).getValue(), is("@new_contact"));
        assertThat(performed.getVariables().get(1).getValue(), is("group-M"));
    }
}
