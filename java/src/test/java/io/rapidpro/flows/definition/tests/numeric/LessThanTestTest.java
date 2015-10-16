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
 * Test for {@link LessThanTest}
 */
public class LessThanTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "lt", "test", "32");
        LessThanTest test = (LessThanTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_test, is("32"));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        LessThanTest test = new LessThanTest("32 ");
        assertTest(test, "3l", true, new BigDecimal(31));
        assertTest(test, "32", false, null);
        assertTest(test, "33", false, null);

        // test can be an expression
        test = new LessThanTest("@(contact.age - 2)");

        assertTest(test, "3l", true, new BigDecimal(31));
        assertTest(test, "32", false, null);
        assertTest(test, "33", false, null);
    }
}
