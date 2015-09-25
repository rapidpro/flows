package io.rapidpro.flows.definition.tests.text;

import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

/**
 * Test for {@link StartsWithTest}
 */
public class StartsWithTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        StartsWithTest test = new StartsWithTest(new TranslatableText("once"));

        assertTest(test, "  ONCE", true, "ONCE");
        assertTest(test, "Once upon a time", true, "Once");

        assertTest(test, "Hey once", false, null);
    }
}
