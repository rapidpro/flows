package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link AndTest}
 */
public class BetweenTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        BetweenTest test = new BetweenTest("32 ", "4l");

        assertTest(test, "32 cats", true, "32");
        assertTest(test, "4l dogs", true, "41");
        assertTest(test, "31", false, null);
        assertTest(test, "42", false, null);

        // min and max can be expressions
        test = new BetweenTest("@contact.age", "@(contact.age + 3)");

        assertTest(test, "35", true, "35");
    }
}
