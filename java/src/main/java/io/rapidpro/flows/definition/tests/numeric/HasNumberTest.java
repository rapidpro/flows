package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Runner;

import java.math.BigDecimal;

/**
 * Test which returns whether input has a number
 */
public class HasNumberTest extends NumericTest {

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static HasNumberTest fromJson(JsonObject obj, Flow.DeserializationContext context) {
        return new HasNumberTest();
    }

    /**
     * @see NumericTest#evaluateAgainstDecimal(Runner, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateAgainstDecimal(Runner runner, EvaluationContext context, BigDecimal decimal) {
        return true; // this method is only called on decimals parsed from the input
    }
}
