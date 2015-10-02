package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test for {@link GreaterThanTest}
 */
public class GreaterThanTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        GreaterThanTest test = new GreaterThanTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", false, null);
        assertTest(test, "33", true, "33", new BigDecimal(33));

        // test can be an expression
        test = new GreaterThanTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", false, null);
        assertTest(test, "33", true, "33", new BigDecimal(33));
    }
}
