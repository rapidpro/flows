package io.rapidpro.flows.definition.actions.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Sends a message to the contact
 */
public class ReplyAction extends MessageAction {

    public static final String TYPE = "reply";

    public ReplyAction(TranslatableText msg) {
        super(msg);
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static ReplyAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new ReplyAction(TranslatableText.fromJson(obj.get("msg")));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "msg", m_msg.toJson());
    }

    /**
     * @see MessageAction#executeWithMessage(Runner, EvaluationContext, String)
     */
    @Override
    protected Result executeWithMessage(Runner runner, EvaluationContext context, String message) {
        EvaluatedTemplate template = runner.substituteVariables(message, context);
        Action performed = new ReplyAction(new TranslatableText(template.getOutput()));
        return Result.performed(performed, template.getErrors());
    }
}
