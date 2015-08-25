package io.rapidpro.flows.definition.actions;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

/**
 * Sends a message to the contact
 */
public class ReplyAction extends Action {

    @SerializedName("msg")
    protected TranslatableText m_msg;

    public ReplyAction(TranslatableText msg) {
        m_msg = msg;
    }

    /**
     * @see Action#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static ReplyAction fromJson(JsonObject json, Flow.DeserializationContext context) throws FlowParseException {
        TranslatableText msg = TranslatableText.fromJson(json.get("msg"));
        return new ReplyAction(msg);
    }

    /**
     * @see Action#execute(Flows.Runner, RunState, Input)
     */
    @Override
    public Result execute(Flows.Runner runner, RunState run, Input input) {
        String msg = m_msg.getLocalized(run);
        if (StringUtils.isNotEmpty(msg)) {
            EvaluatedTemplate template = run.substituteVariables(msg, run.buildContext(input));
            Action performed = new ReplyAction(new TranslatableText(template.getOutput()));
            return new Result(performed, template.getErrors());
        }
        return Result.NOOP;
    }

    public TranslatableText getMsg() {
        return m_msg;
    }
}
