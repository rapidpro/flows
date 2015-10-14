package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link BetweenTest}
 */
public class BetweenTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        BetweenTest test = BetweenTest.fromJson(parseObject("{\"min\": \"@foo\", \"max\": \"123\"}"), m_deserializationContext);
        assertThat(test.m_min, is("@foo"));
        assertThat(test.m_max, is("123"));
    }

    @Test
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
