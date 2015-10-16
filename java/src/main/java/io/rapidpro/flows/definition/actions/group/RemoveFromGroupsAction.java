package io.rapidpro.flows.definition.actions.group;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.List;

/**
 * Removes the contact from one or more groups
 */
public class RemoveFromGroupsAction extends GroupMembershipAction {

    public static final String TYPE = "del_group";

    public RemoveFromGroupsAction(List<GroupRef> groups) {
        super(groups);
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static RemoveFromGroupsAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new RemoveFromGroupsAction(JsonUtils.fromJsonArray(obj.get("groups").getAsJsonArray(), context, GroupRef.class));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "groups", JsonUtils.toJsonArray(m_groups));
    }

    /**
     * @see GroupMembershipAction#executeWithGroups(Runner, RunState, List, List)
     */
    @Override
    protected Result executeWithGroups(Runner runner, RunState run, List<GroupRef> groups, List<String> errors) {
        if (groups.size() > 0) {
            for (GroupRef group : groups) {
                run.getContact().getGroups().remove(group.getName());
            }
            return Result.performed(new RemoveFromGroupsAction(groups));
        } else {
            return Result.errors(errors);
        }
    }
}
