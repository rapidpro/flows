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
 * Test for {@link EqualTest}
 */
public class EqualTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "eq", "test", "32");
        EqualTest test = (EqualTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_test, is("32"));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        EqualTest test = new EqualTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", false, null);

        // test can be an expression
        test = new EqualTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, new BigDecimal(32));
        assertTest(test, "33", false, null);
    }
}
