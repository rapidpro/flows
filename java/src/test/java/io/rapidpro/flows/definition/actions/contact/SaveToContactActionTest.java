package io.rapidpro.flows.definition.actions.contact;

import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SaveToContactAction}
 */
public class SaveToContactActionTest extends BaseActionTest {

    @Test
    public void fromJson() {
        SaveToContactAction action = (SaveToContactAction) JsonUtils.getGson().fromJson("{\"type\":\"save\",\"field\":\"age\",\"label\":\"Age\",\"value\":\"@extra.age\"}", Action.class);

        assertThat(action.getField(), is("age"));
        assertThat(action.getLabel(), is("Age"));
        assertThat(action.getValue(), is("@extra.age"));
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
