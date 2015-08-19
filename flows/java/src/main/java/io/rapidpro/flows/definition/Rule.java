package io.rapidpro.flows.definition;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 *
 */
public class Rule implements Flow.ConnectionStart {

    protected Test m_test;

    protected Flow.Node m_destination;

    public static Rule fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) throws JsonSyntaxException {
        Rule obj = new Rule();
        obj.m_test = Test.fromJson(json.get("test").getAsJsonObject());

        String destinationUuid = FlowUtils.getAsString(json, "destination");
        if (StringUtils.isNotEmpty(destinationUuid)) {
            destinationsToSet.put(obj, destinationUuid);
        }
        return obj;
    }

    public Test.Result matches(RunState run, EvaluationContext context, String input) {
        return m_test.evaluate(run, context, input);
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
