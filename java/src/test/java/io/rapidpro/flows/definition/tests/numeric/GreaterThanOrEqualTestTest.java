package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test for {@link GreaterThanOrEqualTest}
 */
public class GreaterThanOrEqualTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        GreaterThanOrEqualTest test = new GreaterThanOrEqualTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, "32", new BigDecimal(32));
        assertTest(test, "33", true, "33", new BigDecimal(33));

        // test can be an expression
        test = new GreaterThanOrEqualTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, "32", new BigDecimal(32));
        assertTest(test, "33", true, "33", new BigDecimal(33));
    }
}
