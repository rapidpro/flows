package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.RunState;

/**
 * Test that always returns true
 */
public class TrueTest extends Test {
    public static TrueTest fromJson(JsonObject json) {
        return new TrueTest();
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        return Result.textMatch(text);
    }
}
