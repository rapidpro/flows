package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link LessThanOrEqualTest}
 */
public class LessThanOrEqualTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "lte", "test", "32");
        LessThanOrEqualTest test = (LessThanOrEqualTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_test, is("32"));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        LessThanOrEqualTest test = new LessThanOrEqualTest("32 ");
        assertTest(test, "3l", true, new BigDecimal(31));
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", false, null);

        // test can be an expression
        test = new LessThanOrEqualTest("@(contact.age - 2)");

        assertTest(test, "3l", true, new BigDecimal(31));
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", false, null);
    }
}
