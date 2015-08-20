package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.runner.FlowStep;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ActionSet extends Flow.Node implements Flow.ConnectionStart {

    protected static Logger logger = LoggerFactory.getLogger(ActionSet.class);

    protected List<Action> m_actions = new ArrayList<>();

    protected Flow.Node m_destination;

    public static ActionSet fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) throws JsonSyntaxException {
        ActionSet obj = new ActionSet();
        obj.m_uuid = json.get("uuid").getAsString();

        String destinationUuid = FlowUtils.getAsString(json, "destination");
        if (StringUtils.isNotEmpty(destinationUuid)) {
            destinationsToSet.put(obj, destinationUuid);
        }

        for (JsonElement actionElem : json.get("actions").getAsJsonArray()) {
            obj.m_actions.add(Action.fromJson(actionElem.getAsJsonObject()));
        }

        return obj;
    }

    @Override
    public Flow.Node visit(RunState run, FlowStep step, String input) {
        if (logger.isDebugEnabled()) {
            logger.debug("Visiting action set " + m_uuid + " with input " + input + " from contact " + run.getContact().getUuid());
        }

        for (Action action : m_actions) {
            Action.Result result = action.execute(run);
            if (result.m_action != null) {
                step.getActions().add(result);
            }
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
