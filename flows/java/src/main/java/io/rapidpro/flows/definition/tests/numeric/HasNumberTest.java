package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.RunState;

import java.math.BigDecimal;

/**
 * Test which returns whether input has a number
 */
public class HasNumberTest extends NumericTest {

    public static HasNumberTest fromJson(JsonObject obj) {
        return new HasNumberTest();
    }

    /**
     * @see NumericTest#evaluateAgainstDecimal(RunState, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateAgainstDecimal(RunState run, EvaluationContext context, BigDecimal decimal) {
        return true; // this method is only called on decimals parsed from the input
    }
}
