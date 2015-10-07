package io.rapidpro.flows.definition.actions.message;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Runner;

/**
 * Sends a message to the contact
 */
public class ReplyAction extends MessageAction {

    public static final String TYPE = "reply";

    public ReplyAction(TranslatableText msg) {
        super(TYPE, msg);
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
