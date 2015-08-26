package io.rapidpro.flows.definition.actions;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Group;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the contact to one or more groups
 */
public class AddToGroupAction extends Action {

    protected static final String TYPE = "add_group";

    @SerializedName("groups")
    protected List<Group> m_groups;

    public AddToGroupAction(List<Group> groups) {
        super(TYPE);
        m_groups = groups;
    }

    /**
     * @see Action#execute(Flows.Runner, RunState, Input)
     */
    @Override
    public Result execute(Flows.Runner runner, RunState run, Input input) {
        EvaluationContext context = run.buildContext(input);
        List<Group> groups = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Group group : m_groups) {
            if (group.getId() == null) {
                EvaluatedTemplate template = run.substituteVariables(group.getName(), context);
                if (!template.hasErrors()) {
                    run.getContact().getGroups().add(template.getOutput());
                    groups.add(new Group(template.getOutput()));
                } else {
                    errors.add(group.getName());
                }
            } else {
                run.getContact().getGroups().add(group.getName());
                groups.add(group);
            }
        }

        Action performed = groups.size() > 0 ? new AddToGroupAction(groups) : null;
        return new Result(performed, errors);
    }

    public List<Group> getGroups() {
        return m_groups;
    }
}
