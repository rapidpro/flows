package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import org.threeten.bp.LocalDate;

/**
 * Test which returns whether input is a date equal to the given value
 */
public class DateEqualTest extends DateComparisonTest {

    public DateEqualTest(String test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static DateEqualTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new DateEqualTest(obj.get("test").getAsString());
    }

    /**
     * @see DateComparisonTest#doComparison(LocalDate, LocalDate)
     */
    @Override
    protected boolean doComparison(LocalDate input, LocalDate test) {
        return input.compareTo(test) == 0;
    }
}
