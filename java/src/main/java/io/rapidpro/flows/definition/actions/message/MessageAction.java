package io.rapidpro.flows.definition.actions.message;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for actions which send a message
 */
public abstract class MessageAction extends Action {

    protected TranslatableText m_msg;

    public MessageAction(TranslatableText msg) {
        m_msg = msg;
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        String msg = m_msg.getLocalized(run);
        if (StringUtils.isNotEmpty(msg)) {
            EvaluationContext context = run.buildContext(runner, input);
            return executeWithMessage(runner, context, msg);
        } else {
            return Result.NOOP;
        }
    }

    protected abstract Result executeWithMessage(Runner runner, EvaluationContext context, String message);

    public TranslatableText getMsg() {
        return m_msg;
    }
}
