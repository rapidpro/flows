package io.rapidpro.flows.definition.tests.location;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Location;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

/**
 * Test that returns whether the text contains the valid state
 */
public class HasStateTest extends Test {

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static HasStateTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new HasStateTest();
    }

    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        String country = run.getOrg().getCountry();
        if (StringUtils.isNotEmpty(country)) {
            Location location = run.getLocationResolver().resolve(text, country, Location.Level.STATE, null);
            if (location != null) {
                return Result.textMatch(location.getName());
            }
        }

        return Result.NO_MATCH;
    }
}
