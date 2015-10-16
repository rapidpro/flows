package io.rapidpro.flows.definition.tests.location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Test that returns whether the text contains the valid state
 */
public class HasStateTest extends Test {

    public static final String TYPE = "state";

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static HasStateTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new HasStateTest();
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE);
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        String country = run.getOrg().getCountry();
        if (StringUtils.isNotEmpty(country)) {
            Location state = runner.parseLocation(text, country, Location.Level.STATE, null);
            if (state != null) {
                return Result.match(state.getName());
            }
        }

        return Result.NO_MATCH;
    }
}
