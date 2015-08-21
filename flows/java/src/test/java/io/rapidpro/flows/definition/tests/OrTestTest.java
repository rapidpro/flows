package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.TranslatableText;

import java.util.Arrays;

/**
 * Test for {@link OrTest}
 */
public class OrTestTest extends BaseTestTest {
    @org.junit.Test
    public void evaluate() {
        OrTest test = new OrTest(Arrays.<Test>asList(
                new ContainsTest(new TranslatableText("upon")),
                new StartsWithTest(new TranslatableText("once"))
        ));
        assertTest(test, "Once upon a time", true, "upon");
        assertTest(test, "Once a time", true, "Once");
        assertTest(test, "upon this rock I once", true, "upon");
        assertTest(test, "huh", false, null);
    }
}
