package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.FlowParseException;

import java.math.BigDecimal;

/**
 * Test which returns whether input is numerically equal a value
 */
public class EqualTest extends ComparisonTest {

    public EqualTest(String test) {
        super(test);
    }

    public static EqualTest fromJson(JsonObject obj) throws FlowParseException {
        return new EqualTest(obj.get("test").getAsString());
    }

    /**
     * @see ComparisonTest#doComparison(BigDecimal, BigDecimal)
     */
    @Override
    protected boolean doComparison(BigDecimal input, BigDecimal test) {
        return input.compareTo(test) == 0;
    }
}
