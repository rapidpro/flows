package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.FlowParseException;

import java.math.BigDecimal;

/**
 * Test which returns whether input is numerically less than or equal to a value
 */
public class LessThanOrEqualTest extends ComparisonTest {

    public LessThanOrEqualTest(String test) {
        super(test);
    }

    public static LessThanOrEqualTest fromJson(JsonObject obj) throws FlowParseException {
        return new LessThanOrEqualTest(obj.get("test").getAsString());
    }

    /**
     * @see ComparisonTest#doComparison(BigDecimal, BigDecimal)
     */
    @Override
    protected boolean doComparison(BigDecimal input, BigDecimal test) {
        return input.compareTo(test) <= 0;
    }
}
