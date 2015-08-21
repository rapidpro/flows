package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link LessThanOrEqualTest}
 */
public class LessThanOrEqualTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        LessThanOrEqualTest test = new LessThanOrEqualTest("32 ");
        assertTest(test, "3l", true, "31");
        assertTest(test, "32", true, "32");
        assertTest(test, "33", false, null);

        // test can be an expression
        test = new LessThanOrEqualTest("@(contact.age - 2)");

        assertTest(test, "3l", true, "31");
        assertTest(test, "32", true, "32");
        assertTest(test, "33", false, null);
    }
}
