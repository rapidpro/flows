package io.rapidpro.flows.definition.actions;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

/**
 * Sets the contact's language
 */
public class SetLanguageAction extends Action {

    public static final String TYPE = "lang";

    @SerializedName("lang")
    protected String m_lang;

    @SerializedName("name")
    protected String m_name;

    public SetLanguageAction(String lang, String name) {
        super(TYPE);
        m_lang = lang;
        m_name = name;
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        run.getContact().setLanguage(m_lang);
        return new Result(new SetLanguageAction(m_lang, m_name));
    }

    public String getLang() {
        return m_lang;
    }

    public String getName() {
        return m_name;
    }
}
