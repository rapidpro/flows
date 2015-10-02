package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;

import java.math.BigDecimal;

/**
 * Test which returns whether input is numerically greater than or equal to a value
 */
public class GreaterThanOrEqualTest extends NumericComparisonTest {

    public static final String TYPE = "gte";

    public GreaterThanOrEqualTest(String test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static GreaterThanOrEqualTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new GreaterThanOrEqualTest(obj.get("test").getAsString());
    }

    /**
     * @see NumericComparisonTest#doComparison(BigDecimal, BigDecimal)
     */
    @Override
    protected boolean doComparison(BigDecimal input, BigDecimal test) {
        return input.compareTo(test) >= 0;
    }
}
