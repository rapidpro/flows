package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test for {@link LessThanOrEqualTest}
 */
public class LessThanOrEqualTestTest extends BaseTestTest {
    @Test
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
