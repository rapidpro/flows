package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link EqualTest}
 */
public class EqualTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        EqualTest test = new EqualTest("32 ");
        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, "32");
        assertTest(test, "33", false, null);

        // test can be an expression
        test = new EqualTest("@(contact.age - 2)");

        assertTest(test, "3l", false, null);
        assertTest(test, "32", true, "32");
        assertTest(test, "33", false, null);
    }
}
