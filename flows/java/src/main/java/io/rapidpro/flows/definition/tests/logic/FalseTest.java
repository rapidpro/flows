package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;

/**
 * Test that always returns false
 */
public class FalseTest extends Test {

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static FalseTest fromJson(JsonObject obj, Flow.DeserializationContext context) {
        return new FalseTest();
    }

    /**
     * @see Test#evaluate(Flows.Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Flows.Runner runner, RunState run, EvaluationContext context, String text) {
        return new Result(false, text);
    }
}
