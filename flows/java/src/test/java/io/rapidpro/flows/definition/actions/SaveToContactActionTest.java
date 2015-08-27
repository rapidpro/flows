package io.rapidpro.flows.definition.actions;

import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
        m_run.getExtra().put("age", "64");
        SaveToContactAction action = new SaveToContactAction("age", "Age", "@extra.age");

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        SaveToContactAction performed = (SaveToContactAction) result.getActionPerformed();

        assertThat(performed.getField(), is("age"));
        assertThat(performed.getLabel(), is("Age"));
        assertThat(performed.getValue(), is("64"));
        assertThat(result.getErrors(), empty());

        assertThat(m_run.getContact().getFields().get("age"), is("64"));

        // NOOP for invalid expression
        action = new SaveToContactAction("age", "Age", "@badexpression");
        result = action.execute(m_runner, m_run, Input.of("Yes"));

        assertThat(result.getActionPerformed(), is(nullValue()));
        assertThat(m_run.getContact().getFields().get("age"), is("64"));
    }
}
