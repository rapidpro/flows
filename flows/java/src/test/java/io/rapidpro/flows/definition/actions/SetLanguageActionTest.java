package io.rapidpro.flows.definition.actions;

import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SetLanguageAction}
 */
public class SetLanguageActionTest extends BaseActionTest {

    @Test
    public void fromJson() {
        SetLanguageAction action = (SetLanguageAction) JsonUtils.getGson().fromJson("{\"type\":\"lang\",\"lang\":\"fre\",\"name\":\"Français\"}", Action.class);

        assertThat(action.getLang(), is("fre"));
        assertThat(action.getName(), is("Français"));
    }

    @Test
    public void execute() {
        SetLanguageAction action = new SetLanguageAction("fre", "Français");

        Action.Result result = action.execute(m_runner, m_run, Input.of("Yes"));
        SetLanguageAction performed = (SetLanguageAction) result.getActionPerformed();

        assertThat(performed.getLang(), is("fre"));
        assertThat(performed.getName(), is("Français"));
    }
}
