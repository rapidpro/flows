package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.TranslatableText;
import org.junit.Test;

/**
 * Test for {@link FalseTest}
 */
public class ContainsTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        ContainsTest test = new ContainsTest(new TranslatableText("north,east"));

        assertTest(test, "go north east", true, "north east");
        assertTest(test, "EAST then NORRTH", true, "NORRTH EAST");

        assertTest(test, "go north", false, null);
        assertTest(test, "east", false, null);
    }
}
