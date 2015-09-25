package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import org.threeten.bp.LocalDate;

/**
 * Test which returns whether input is a date before the given value
 */
public class DateBeforeTest extends DateComparisonTest {

    public DateBeforeTest(String test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static DateBeforeTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new DateBeforeTest(obj.get("test").getAsString());
    }

    /**
     * @see DateComparisonTest#doComparison(LocalDate, LocalDate)
     */
    @Override
    protected boolean doComparison(LocalDate input, LocalDate test) {
        return input.compareTo(test) <= 0;
    }
}
