package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link GreaterThanOrEqualTest}
 */
public class GreaterThanOrEqualTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "gte", "test", "32");
        GreaterThanOrEqualTest test = (GreaterThanOrEqualTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is("32"));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        GreaterThanOrEqualTest test = new GreaterThanOrEqualTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", true, new BigDecimal(33));

        // test can be an expression
        test = new GreaterThanOrEqualTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", true, new BigDecimal(33));
    }
}
