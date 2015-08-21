package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.FlowParseException;

import java.math.BigDecimal;

/**
 * Test which returns whether input is numerically greater than a value
 */
public class GreaterThanTest extends ComparisonTest {

    public GreaterThanTest(String test) {
        super(test);
    }

    public static GreaterThanTest fromJson(JsonObject obj) throws FlowParseException {
        return new GreaterThanTest(obj.get("test").getAsString());
    }

    /**
     * @see ComparisonTest#doComparison(BigDecimal, BigDecimal)
     */
    @Override
    protected boolean doComparison(BigDecimal input, BigDecimal test) {
        return input.compareTo(test) > 0;
    }
}
