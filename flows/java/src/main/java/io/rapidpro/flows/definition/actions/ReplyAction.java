package io.rapidpro.flows.definition.actions;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.apache.commons.lang3.StringUtils;

/**
 * Sends a message to the contact
 */
public class ReplyAction extends Action {

    protected static final String TYPE = "reply";

    @SerializedName("msg")
    protected TranslatableText m_msg;

    public ReplyAction(TranslatableText msg) {
        super(TYPE);
        m_msg = msg;
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        String msg = m_msg.getLocalized(run);
        if (StringUtils.isNotEmpty(msg)) {
            EvaluatedTemplate template = runner.substituteVariables(msg, run.buildContext(input));
            Action performed = new ReplyAction(new TranslatableText(template.getOutput()));
            return new Result(performed, template.getErrors());
        }
        return Result.NOOP;
    }

    public TranslatableText getMsg() {
        return m_msg;
    }
}
