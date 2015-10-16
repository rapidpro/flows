package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

/**
 * Test which returns whether input is a number between two numbers (inclusive)
 */
public class BetweenTest extends NumericTest {

    public static final String TYPE = "between";

    protected String m_min;

    protected String m_max;

    public BetweenTest(String min, String max) {
        m_min = min;
        m_max = max;
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static BetweenTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new BetweenTest(obj.get("min").getAsString(), obj.get("max").getAsString());
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "min", m_min, "max", m_max);
    }

    /**
     * @see NumericTest#evaluateForDecimal(Runner, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateForDecimal(Runner runner, EvaluationContext context, BigDecimal decimal) {
        EvaluatedTemplate min = runner.substituteVariables(m_min, context);
        EvaluatedTemplate max = runner.substituteVariables(m_max, context);

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

    public String getMin() {
        return m_min;
    }

    public String getMax() {
        return m_max;
    }
}
