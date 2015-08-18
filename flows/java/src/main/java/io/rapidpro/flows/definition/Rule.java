package io.rapidpro.flows.definition;

import com.google.gson.JsonObject;
import io.rapidpro.flows.FlowUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 *
 */
public class Rule implements Flow.ConnectionStart {

    protected Test m_test;

    protected Flow.Node m_destination;

    public static Rule fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) {
        Rule obj = new Rule();
        obj.m_test = Test.fromJson(json.get("test").getAsJsonObject());

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
