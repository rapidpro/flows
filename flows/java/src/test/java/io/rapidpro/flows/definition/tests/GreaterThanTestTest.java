package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link GreaterThanTest}
 */
public class GreaterThanTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        GreaterThanTest test = new GreaterThanTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", false, null);
        assertTest(test, "33", true, "33");

        // test can be an expression
        test = new GreaterThanTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", false, null);
        assertTest(test, "33", true, "33");
    }
}
