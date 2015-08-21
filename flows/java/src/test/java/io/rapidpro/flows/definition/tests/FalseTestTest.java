package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link FalseTest}
 */
public class FalseTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        FalseTest test = new FalseTest();
        assertTest(test, "huh?", false, "huh?");
    }
}
