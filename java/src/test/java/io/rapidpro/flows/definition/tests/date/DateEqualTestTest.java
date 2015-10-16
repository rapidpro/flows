package io.rapidpro.flows.definition.tests.date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;
import org.threeten.bp.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link DateEqualTest}
 */
public class DateEqualTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "date_equal", "test", "December 14, 1892");
        DateEqualTest test = (DateEqualTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is("December 14, 1892"));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        DateEqualTest test = new DateEqualTest("24/8/2015");

        assertTest(test, "23-8-15", false, null);
        assertTest(test, "it was Aug 24, 2015", true, LocalDate.of(2015, 8, 24));
        assertTest(test, "25th Aug '15", false, null);

        // date can be an expression
        m_context.putVariable("dob", "24-08-2015");
        test = new DateEqualTest("@(dob)");

        assertTest(test, "23-8-15", false, null);
        assertTest(test, "it was Aug 24, 2015", true, LocalDate.of(2015, 8, 24));
        assertTest(test, "25th Aug '15", false, null);
    }
}
