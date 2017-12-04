package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;
import org.threeten.bp.LocalDate;

/**
 * Test which returns whether input is a date before the given value
 */
public class DateBeforeTest extends DateComparisonTest {

    public static final String TYPE = "date_before";

    public DateBeforeTest(String test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static DateBeforeTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new DateBeforeTest(obj.get("test").getAsString());
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "test", m_test);
    }

    /**
     * @see DateComparisonTest#doComparison(LocalDate, LocalDate)
     */
    @Override
    protected boolean doComparison(LocalDate input, LocalDate test) {
        return input.compareTo(test) <= 0;
    }
}
