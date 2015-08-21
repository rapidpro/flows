package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.RunState;

/**
 * Test that always returns false
 */
public class FalseTest extends Test {
    public static FalseTest fromJson(JsonObject obj) {
        return new FalseTest();
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        return new Result(false, text);
    }
}
