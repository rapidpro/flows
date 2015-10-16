package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

/**
 * Test that returns whether the input is non-empty (and non-blank)
 */
public class NotEmptyTest extends Test {

    public static final String TYPE = "not_empty";

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static NotEmptyTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        return new NotEmptyTest();
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
        text = text.trim();

        if (text.length() > 0) {
            return Result.match(text);
        } else {
            return Result.NO_MATCH;
        }
    }
}
