package io.rapidpro.flows.definition.actions.contact;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.BaseActionTest;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.utils.JsonUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SetLanguageAction}
 */
public class SetLanguageActionTest extends BaseActionTest {

    @Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "lang", "lang", "fre", "name", "Français");
        SetLanguageAction action = (SetLanguageAction) Action.fromJson(elm, m_deserializationContext);
        assertThat(action.getLang(), is("fre"));
        assertThat(action.getName(), is("Français"));

        assertThat(action.toJson(), is(elm));
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

        // check when lang is not 3-letter code
        action = new SetLanguageAction("base", "Default");

        result = action.execute(m_runner, m_run, Input.of("Yes"));
        assertThat(result.getErrors(), empty());

        performed = (SetLanguageAction) result.getPerformed();
        assertThat(performed.getLang(), is("base"));
        assertThat(performed.getName(), is("Default"));

        assertThat(m_run.getContact().getLanguage(), is(nullValue()));
    }
}
