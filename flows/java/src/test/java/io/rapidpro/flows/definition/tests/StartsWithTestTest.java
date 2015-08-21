package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.TranslatableText;
import org.junit.Test;

/**
 * Test for {@link TrueTest}
 */
public class StartsWithTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        StartsWithTest test = new StartsWithTest(new TranslatableText("once"));

        assertTest(test, "ONCE", true, "ONCE");
        assertTest(test, "Once upon a time", true, "Once");

        assertTest(test, "Hey once", false, null);
    }
}
