package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import org.threeten.bp.LocalDate;

/**
 * Test that returns whether the text contains the a valid date
 */
public class HasDateTest extends DateTest {

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static HasDateTest fromJson(JsonObject obj, Flow.DeserializationContext context) {
        return new HasDateTest();
    }

    /**
     * @see DateTest#evaluateAgainstDate(RunState, EvaluationContext, LocalDate)
     */
    @Override
    protected boolean evaluateAgainstDate(RunState run, EvaluationContext context, LocalDate date) {
        return true;
    }
}
