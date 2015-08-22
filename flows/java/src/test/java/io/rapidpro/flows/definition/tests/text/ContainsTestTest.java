package io.rapidpro.flows.definition.tests.text;

import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

/**
 * Test for {@link ContainsTest}
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
