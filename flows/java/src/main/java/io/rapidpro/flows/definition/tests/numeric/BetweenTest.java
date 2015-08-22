package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;

import java.math.BigDecimal;

/**
 * Test which returns whether input is a number between two numbers (inclusive)
 */
public class BetweenTest extends NumericTest {

    protected String m_min;

    protected String m_max;

    public BetweenTest(String min, String max) {
        m_min = min;
        m_max = max;
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static BetweenTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new BetweenTest(obj.get("min").getAsString(), obj.get("max").getAsString());
    }

    /**
     * @see NumericTest#evaluateAgainstDecimal(RunState, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateAgainstDecimal(RunState run, EvaluationContext context, BigDecimal decimal) {
        EvaluatedTemplate min = run.substituteVariables(m_min, context);
        EvaluatedTemplate max = run.substituteVariables(m_max, context);

        if (!min.hasErrors() && !max.hasErrors()) {
            try {
                BigDecimal minVal = new BigDecimal(min.getOutput().trim());
                BigDecimal maxVal = new BigDecimal(max.getOutput().trim());

                return decimal.compareTo(minVal) >= 0 && decimal.compareTo(maxVal) <= 0;
            }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }
}
