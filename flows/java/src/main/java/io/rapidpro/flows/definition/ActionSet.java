package io.rapidpro.flows.definition;

import com.google.gson.JsonObject;
import io.rapidpro.flows.FlowUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 *
 */
public class ActionSet extends Flow.Node implements Flow.ConnectionStart {

    protected Flow.Node m_destination;

    public static ActionSet fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) {
        ActionSet obj = new ActionSet();
        obj.m_uuid = json.get("uuid").getAsString();

        String destinationUuid = FlowUtils.getAsString(json, "destination");
        if (StringUtils.isNotEmpty(destinationUuid)) {
            destinationsToSet.put(obj, destinationUuid);
        }
        return obj;
    }

    @Override
    public void setDestination(Flow.Node destination) {
        this.m_destination = destination;
    }
}
