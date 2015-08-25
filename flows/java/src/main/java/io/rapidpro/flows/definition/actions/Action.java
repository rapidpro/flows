package io.rapidpro.flows.definition.actions;

import com.google.gson.JsonObject;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An action which can be performed inside an action set
 */
public abstract class Action {

    protected static Map<String, Class<? extends Action>> s_classByType = new HashMap<>();
    static {
        s_classByType.put("reply", ReplyAction.class);
        s_classByType.put("add_group", AddToGroupAction.class);
    }

    /**
     * Executes this action
     * @param runner the flow runner
     * @param run the current run state
     * @param input the current input
     * @return the action result (action that was actually performed and any errors)
     */
    public abstract Result execute(Flows.Runner runner, RunState run, Input input);

    /**
     * Creates an action from the given JSON object
     * @param obj the JSON object
     * @param context the deserialization context
     * @return the action
     */
    public static Action fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        String type = obj.get("type").getAsString();
        Class<? extends Action> clazz = s_classByType.get(type);
        if (clazz == null) {
            throw new FlowParseException("Unknown action type: " + type);
        }

        return JsonUtils.fromJson(obj, context, clazz);
    }

    /**
     * Holds the result of an action execution
     */
    public static class Result {
        public static final Result NOOP = new Result(null, null);

        protected Action m_action;

        protected List<String> m_errors;

        public Result(Action action) {
            this(action, Collections.<String>emptyList());
        }

        public Result(Action action, List<String> errors) {
            m_action = action;
            m_errors = errors;
        }

        public Action getAction() {
            return m_action;
        }

        public List<String> getErrors() {
            return m_errors;
        }

        public boolean hasErrors() {
            return !m_errors.isEmpty();
        }
    }
}
