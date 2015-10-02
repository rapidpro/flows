package io.rapidpro.flows.definition.tests.logic;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

/**
 * Test for {@link TrueTest}
 */
public class TrueTestTest extends BaseTestTest {

    @Test
    public void evaluate() {
        TrueTest test = new TrueTest();
        assertTest(test, "huh?", true, "huh?");
    }
}
