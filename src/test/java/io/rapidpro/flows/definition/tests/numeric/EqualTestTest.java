package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
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
        JsonElement elm = JsonUtils.object("type", "eq", "test", "32");
        EqualTest test = (EqualTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is("32"));

        assertThat(test.toJson(), is(elm));
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
