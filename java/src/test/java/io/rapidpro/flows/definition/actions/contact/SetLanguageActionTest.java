package io.rapidpro.flows.definition.actions.contact;

import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
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
        assertThat(result.getErrors(), empty());

        SetLanguageAction performed = (SetLanguageAction) result.getPerformed();
        assertThat(performed.getLang(), is("fre"));
        assertThat(performed.getName(), is("Français"));

        assertThat(m_run.getContact().getLanguage(), is("fre"));
    }
}
