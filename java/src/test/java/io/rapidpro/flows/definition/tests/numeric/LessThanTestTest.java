package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test for {@link LessThanTest}
 */
public class LessThanTestTest extends BaseTestTest {
    @Test
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
