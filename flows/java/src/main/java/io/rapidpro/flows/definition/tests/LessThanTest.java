package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.FlowParseException;

import java.math.BigDecimal;

/**
 * Test which returns whether input is numerically less than a value
 */
public class LessThanTest extends ComparisonTest {

    public LessThanTest(String test) {
        super(test);
    }

    public static LessThanTest fromJson(JsonObject obj) throws FlowParseException {
        return new LessThanTest(obj.get("test").getAsString());
    }

    /**
     * @see ComparisonTest#doComparison(BigDecimal, BigDecimal)
     */
    @Override
    protected boolean doComparison(BigDecimal input, BigDecimal test) {
        return input.compareTo(test) < 0;
    }
}
