package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test for {@link HasNumberTest}
 */
public class HasNumberTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        HasNumberTest test = new HasNumberTest();

        assertTest(test, "32 cats", true, new BigDecimal(32));
        assertTest(test, "4l dogs", true, new BigDecimal(41));
        assertTest(test, "cats", false, null);
        assertTest(test, "dogs", false, null);
    }
}
