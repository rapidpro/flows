package io.rapidpro.flows.definition.actions.group;

import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.List;

/**
 * Removes the contact from one or more groups
 */
public class RemoveFromGroupsAction extends GroupMembershipAction {

    public static final String TYPE = "del_group";

    public RemoveFromGroupsAction(List<GroupRef> groups) {
        super(TYPE, groups);
    }

    /**
     * @see GroupMembershipAction#executeWithGroups(Runner, RunState, List, List)
     */
    @Override
    protected Result executeWithGroups(Runner runner, RunState run, List<GroupRef> groups, List<String> errors) {
        for (GroupRef group : groups) {
            run.getContact().getGroups().remove(group.getName());
        }

        Action performed = groups.size() > 0 ? new RemoveFromGroupsAction(groups) : null;
        return new Result(performed, errors);
    }
}
