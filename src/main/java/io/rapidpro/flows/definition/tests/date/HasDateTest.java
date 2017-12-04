package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonElement;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;
import org.threeten.bp.LocalDate;

/**
 * Test that returns whether the text contains a valid date
 */
public class HasDateTest extends DateTest {

    public static final String TYPE = "date";

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static HasDateTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        return new HasDateTest();
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE);
    }

    /**
     * @see DateTest#evaluateForDate(Runner, EvaluationContext, LocalDate)
     */
    @Override
    protected boolean evaluateForDate(Runner runner, EvaluationContext context, LocalDate date) {
        return true;
    }
}
