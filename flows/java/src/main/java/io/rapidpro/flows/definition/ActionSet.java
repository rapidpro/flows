package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A flow node which is a set of actions to be performed
 */
public class ActionSet extends Flow.Node implements Flow.ConnectionStart {

    protected static Logger logger = LoggerFactory.getLogger(ActionSet.class);

    protected List<Action> m_actions = new ArrayList<>();

    protected Flow.Node m_destination;

    /**
     * Creates an action set from a JSON object
     * @param obj the JSON object
     * @param context the deserialization context
     * @return the action set
     */
    public static ActionSet fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        ActionSet set = new ActionSet();
        set.m_uuid = obj.get("uuid").getAsString();

        String destinationUuid = JsonUtils.getAsString(obj, "destination");
        if (StringUtils.isNotEmpty(destinationUuid)) {
            context.needsDestination(set, destinationUuid);
        }

        for (JsonElement actionElem : obj.get("actions").getAsJsonArray()) {
            set.m_actions.add(Action.fromJson(actionElem.getAsJsonObject(), context));
        }

        return set;
    }

    /**
     * @see io.rapidpro.flows.definition.Flow.Node#visit(RunState, Step, Input)
     */
    @Override
    public Flow.Node visit(RunState run, Step step, Input input) {
        if (logger.isDebugEnabled()) {
            logger.debug("Visiting action set " + m_uuid + " with input " + input + " from contact " + run.getContact().getUuid());
        }

        for (Action action : m_actions) {
            Action.Result result = action.execute(run, input);
            step.addActionResult(result);
        }

        return m_destination;
    }

    public List<Action> getActions() {
        return m_actions;
    }

    @Override
    public Flow.Node getDestination() {
        return m_destination;
    }

    @Override
    public void setDestination(Flow.Node destination) {
        this.m_destination = destination;
    }
}
