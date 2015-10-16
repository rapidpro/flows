package io.rapidpro.flows.definition.actions.contact;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Sets the contact's language
 */
public class SetLanguageAction extends Action {

    public static final String TYPE = "lang";

    protected String m_lang;

    protected String m_name;

    public SetLanguageAction(String lang, String name) {
        m_lang = lang;
        m_name = name;
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static SetLanguageAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new SetLanguageAction(JsonUtils.getAsString(obj, "lang"), JsonUtils.getAsString(obj, "name"));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "lang", m_lang, "name", m_name);
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        run.getContact().setLanguage(m_lang);
        return Result.performed(new SetLanguageAction(m_lang, m_name));
    }

    public String getLang() {
        return m_lang;
    }

    public String getName() {
        return m_name;
    }
}
