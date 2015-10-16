package io.rapidpro.flows.definition.actions.contact;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.definition.tests.date.DateAfterTest;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SaveToContactAction}
 */
public class SaveToContactActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "save", "field", "age", "label", "Age", "value", "@extra.age");
        SaveToContactAction action = (SaveToContactAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.m_field, is("age"));
        assertThat(action.m_label, is("Age"));
        assertThat(action.m_value, is("@extra.age"));

        assertThat(action.toJson(), is(elm));
    }

    @Test
    public void execute() {
        // update existing field
        SaveToContactAction action = new SaveToContactAction("age", "Age", "@extra.age");
        m_run.getExtra().put("age", "64");

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        SaveToContactAction performed = (SaveToContactAction) result.getPerformed();
        assertThat(performed.getField(), is("age"));
        assertThat(performed.getLabel(), is("Age"));
        assertThat(performed.getValue(), is("64"));

        assertThat(m_run.getContact().getFields().get("age"), is("64"));

        // update new field (no key provided)
        action = new SaveToContactAction(null, "Is OK", "Yes");

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        performed = (SaveToContactAction) result.getPerformed();
        assertThat(performed.getField(), is("is_ok"));
        assertThat(performed.getLabel(), is("Is OK"));
        assertThat(performed.getValue(), is("Yes"));

        assertThat(m_run.getContact().getFields().get("is_ok"), is("Yes"));

        // NOOP for invalid expression
        action = new SaveToContactAction("age", "Age", "@(badexpression)");
        result = action.execute(m_runner, m_run, Input.of("Yes"));

        assertThat(result.getPerformed(), is(nullValue()));
        assertThat(result.getErrors(), contains("Undefined variable: badexpression"));

        assertThat(m_run.getContact().getFields().get("age"), is("64"));

        // try one that updates the phone number
        action = new SaveToContactAction("tel_e164", "Phone Number", "@step.value");
        action.execute(m_runner, m_run, Input.of("0788382382"));
        assertThat(m_run.getContact().getUrns(), hasSize(3));
        assertThat(m_run.getContact().getUrns().get(2), is(new ContactUrn(ContactUrn.Scheme.TEL, "+250788382382")));
    }
}
