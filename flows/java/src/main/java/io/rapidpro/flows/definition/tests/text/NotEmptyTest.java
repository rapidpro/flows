package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;

/**
 * Test that returns whether the input is non-empty (and non-blank)
 */
public class NotEmptyTest extends Test {
    public static NotEmptyTest fromJson(JsonObject obj) {
        return new NotEmptyTest();
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        text = text.trim();

        if (text.length() > 0) {
            return Result.textMatch(text);
        } else {
            return Result.NO_MATCH;
        }
    }
}
