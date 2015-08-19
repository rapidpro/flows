package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 *
 */
public abstract class Action {

    protected static Map<String, Class<? extends Action>> s_classByType = new HashMap<>();
    static {
        s_classByType.put("reply", Reply.class);
        s_classByType.put("add_group", AddToGroup.class);
    }

    public abstract Result execute(RunState run);

    public static Action fromJson(JsonObject json) throws JsonSyntaxException {
        String type = json.get("type").getAsString();
        Class<? extends Action> clazz = s_classByType.get(type);
        return FlowUtils.fromJson(json, clazz);
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
    }

    /**
     * Sends a message to the contact
     */
    public static class Reply extends Action {
        protected TranslatableText m_msg;

        public Reply(TranslatableText msg) {
            m_msg = msg;
        }

        public static Reply fromJson(JsonObject json) {
            TranslatableText msg = TranslatableText.fromJson(json.get("msg"));
            return new Reply(msg);
        }

        @Override
        public Result execute(RunState run) {
            String msg = m_msg.getLocalized(run);
            if (StringUtils.isNotEmpty(msg)) {
                EvaluatedTemplate template = run.substituteVariables(msg, run.buildContext());

                return new Result(new Reply(new TranslatableText(template.getOutput())), template.getErrors());
            }
            return Result.NOOP;
        }

        public TranslatableText getMsg() {
            return m_msg;
        }
    }

    /**
     * Adds the contact to one or more groups
     */
    public static class AddToGroup extends Action {
        protected List<String> m_groups;

        public AddToGroup(List<String> groups) {
            m_groups = groups;
        }

        public static AddToGroup fromJson(JsonObject json) {
            List<String> groups = new ArrayList<>();
            for (JsonElement groupElem : json.get("groups").getAsJsonArray()) {
                groups.add(groupElem.getAsJsonObject().get("name").getAsString());
            }
            return new AddToGroup(groups);
        }

        @Override
        public Result execute(RunState run) {
            EvaluationContext context = run.buildContext();
            List<String> groups = new ArrayList<>();
            for (String group : m_groups) {
                EvaluatedTemplate template = run.substituteVariables(group, context);
                if (!template.hasErrors()) {
                    run.getContact().getGroups().add(template.getOutput());
                    groups.add(template.getOutput());
                }

                // TODO how to pass back group name template errors?
            }
            if (groups.size() > 0) {
                return new Result(new AddToGroup(groups));
            } else {
                return Result.NOOP;
            }
        }

        public List<String> getGroups() {
            return m_groups;
        }
    }
}
