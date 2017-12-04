package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link BetweenTest}
 */
public class BetweenTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "between", "min", "@foo", "max", "123");
        BetweenTest test = (BetweenTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getMin(), is("@foo"));
        assertThat(test.getMax(), is("123"));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        BetweenTest test = new BetweenTest("32 ", "41");

        assertTest(test, "32 cats", true, new BigDecimal(32));
        assertTest(test, "4l dogs", true, new BigDecimal(41));
        assertTest(test, "31", false, null);
        assertTest(test, "42", false, null);

        // min and max can be expressions
        test = new BetweenTest("@contact.age", "@(contact.age + 3)");

        assertTest(test, "35", true, new BigDecimal(35));
    }
}
