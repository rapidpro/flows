package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

/**
 * Test which returns whether input has a number
 */
public class HasNumberTest extends NumericTest {

    public static final String TYPE = "number";

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static HasNumberTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        return new HasNumberTest();
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE);
    }

    /**
     * @see NumericTest#evaluateForDecimal(Runner, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateForDecimal(Runner runner, EvaluationContext context, BigDecimal decimal) {
        return true; // this method is only called on decimals parsed from the input
    }
}
