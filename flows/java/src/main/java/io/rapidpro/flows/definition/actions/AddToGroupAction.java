package io.rapidpro.flows.definition.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the contact to one or more groups
 */
public class AddToGroupAction extends Action {
    protected List<String> m_groups;

    public AddToGroupAction(List<String> groups) {
        m_groups = groups;
    }

    public static AddToGroupAction fromJson(JsonObject json) throws FlowParseException {
        List<String> groups = new ArrayList<>();
        for (JsonElement groupElem : json.get("groups").getAsJsonArray()) {
            groups.add(groupElem.getAsJsonObject().get("name").getAsString());
        }
        return new AddToGroupAction(groups);
    }

    /**
     * @see Action#execute(RunState, Input)
     */
    @Override
    public Result execute(RunState run, Input input) {
        EvaluationContext context = run.buildContext(input);
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
            return new Result(new AddToGroupAction(groups));
        } else {
            return Result.NOOP;
        }
    }

    public List<String> getGroups() {
        return m_groups;
    }
}
