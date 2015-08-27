package io.rapidpro.flows.definition.actions.group;

import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.List;

/**
 * Adds the contact to one or more groups
 */
public class AddToGroupsAction extends GroupMembershipAction {

    public static final String TYPE = "add_group";

    public AddToGroupsAction(List<GroupRef> groups) {
        super(TYPE, groups);
    }

    /**
     * @see GroupMembershipAction#executeWithGroups(Runner, RunState, List, List)
     */
    @Override
    protected Action.Result executeWithGroups(Runner runner, RunState run, List<GroupRef> groups, List<String> errors) {
        for (GroupRef group : groups) {
            run.getContact().getGroups().add(group.getName());
        }

        Action performed = groups.size() > 0 ? new AddToGroupsAction(groups) : null;
        return new Action.Result(performed, errors);
    }
}
