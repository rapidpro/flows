package io.rapidpro.flows.definition.actions.group;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for actions which operate on a list of groups
 */
public abstract class GroupMembershipAction extends Action {

    @SerializedName("groups")
    protected List<GroupRef> m_groups;

    public GroupMembershipAction(String type, List<GroupRef> groups) {
        super(type);
        m_groups = groups;
    }

    /**
     * @see Action#execute(Runner, RunState, Input)
     */
    @Override
    public Result execute(Runner runner, RunState run, Input input) {
        EvaluationContext context = run.buildContext(runner, input);
        List<GroupRef> groups = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (GroupRef group : m_groups) {
            if (group.getId() == null) {
                EvaluatedTemplate template = runner.substituteVariables(group.getName(), context);
                if (!template.hasErrors()) {
                    groups.add(new GroupRef(template.getOutput()));
                } else {
                    errors.addAll(template.getErrors());
                }
            } else {
                groups.add(group);
            }
        }

        return executeWithGroups(runner, run, groups, errors);
    }

    protected abstract Result executeWithGroups(Runner runner, RunState run, List<GroupRef> groups, List<String> errors);

    public List<GroupRef> getGroups() {
        return m_groups;
    }
}
