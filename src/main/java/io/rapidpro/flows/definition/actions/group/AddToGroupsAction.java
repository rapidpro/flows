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
 * Adds the contact to one or more groups
 */
public class AddToGroupsAction extends GroupMembershipAction {

    public static final String TYPE = "add_group";

    public AddToGroupsAction(List<GroupRef> groups) {
        super(groups);
    }

    /**
     * @see Action#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static AddToGroupsAction fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new AddToGroupsAction(JsonUtils.fromJsonArray(obj.get("groups").getAsJsonArray(), context, GroupRef.class));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "groups", JsonUtils.toJsonArray(m_groups));
    }

    /**
     * @see GroupMembershipAction#executeWithGroups(Runner, RunState, List, List)
     */
    @Override
    protected Action.Result executeWithGroups(Runner runner, RunState run, List<GroupRef> groups, List<String> errors) {
        if (groups.size() > 0) {
            for (GroupRef group : groups) {
                run.getContact().getGroups().add(group.getName());
            }
            return Result.performed(new AddToGroupsAction(groups), errors);
        } else {
            return Result.errors(errors);
        }
    }
}
