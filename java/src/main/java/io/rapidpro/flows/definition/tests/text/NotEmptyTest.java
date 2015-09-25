package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

/**
 * Test that returns whether the input is non-empty (and non-blank)
 */
public class NotEmptyTest extends Test {

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static NotEmptyTest fromJson(JsonObject obj, Flow.DeserializationContext context) {
        return new NotEmptyTest();
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        text = text.trim();

        if (text.length() > 0) {
            return Result.textMatch(text);
        } else {
            return Result.NO_MATCH;
        }
    }
}
