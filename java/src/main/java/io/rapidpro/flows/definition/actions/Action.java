package io.rapidpro.flows.definition.actions;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.actions.contact.SaveToContactAction;
import io.rapidpro.flows.definition.actions.contact.SetLanguageAction;
import io.rapidpro.flows.definition.actions.group.AddToGroupsAction;
import io.rapidpro.flows.definition.actions.group.RemoveFromGroupsAction;
import io.rapidpro.flows.definition.actions.label.AddLabelsAction;
import io.rapidpro.flows.definition.actions.message.EmailAction;
import io.rapidpro.flows.definition.actions.message.ReplyAction;
import io.rapidpro.flows.definition.actions.message.SendAction;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An action which can be performed inside an action set
 */
public abstract class Action implements Jsonizable {

    protected static Map<String, Class<? extends Action>> s_classByType = new HashMap<>();
    static {
        s_classByType.put(ReplyAction.TYPE, ReplyAction.class);
        s_classByType.put(SendAction.TYPE, SendAction.class);
        s_classByType.put(EmailAction.TYPE, EmailAction.class);
        s_classByType.put(SaveToContactAction.TYPE, SaveToContactAction.class);
        s_classByType.put(SetLanguageAction.TYPE, SetLanguageAction.class);
        s_classByType.put(AddToGroupsAction.TYPE, AddToGroupsAction.class);
        s_classByType.put(RemoveFromGroupsAction.TYPE, RemoveFromGroupsAction.class);
        s_classByType.put(AddLabelsAction.TYPE, AddLabelsAction.class);
    }

    /**
     * Loads an action from the given JSON object
     * @param elm the JSON element
     * @param context the deserialization context
     * @return the test
     */
    public static Action fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        String type = elm.getAsJsonObject().get("type").getAsString();
        Class<? extends Action> clazz = s_classByType.get(type);
        if (clazz == null) {
            throw new FlowParseException("Unknown action type: " + type);
        }

        return JsonUtils.fromJson(elm, context, clazz);
    }

    /**
     * Executes this action
     * @param runner the flow runner
     * @param run the current run state
     * @param input the current input
     * @return the action result (action that was actually performed and any errors)
     */
    public abstract Result execute(Runner runner, RunState run, Input input);

    /**
     * Holds the result of an action execution
     */
    public static class Result {
        public static final Result NOOP = new Result(null, Collections.<String>emptyList());

        protected Action m_performed;

        protected List<String> m_errors;

        protected Result(Action performed, List<String> errors) {
            m_performed = performed;
            m_errors = errors;
        }

        public static Result performed(Action performed) {
            return new Result(performed, Collections.<String>emptyList());
        }

        public static Result performed(Action performed, List<String> errors) {
            return new Result(performed, errors);
        }

        public static Result errors(List<String> errors) {
            return new Result(null, errors);
        }

        public Action getPerformed() {
            return m_performed;
        }

        public List<String> getErrors() {
            return m_errors;
        }

        public boolean hasErrors() {
            return !m_errors.isEmpty();
        }
    }
}
