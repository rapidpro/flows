package io.rapidpro.flows.definition.tests.logic;

import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.definition.tests.text.ContainsTest;
import io.rapidpro.flows.definition.tests.text.StartsWithTest;

import java.util.Arrays;

/**
 * Test for {@link AndTest}
 */
public class AndTestTest extends BaseTestTest {
    @org.junit.Test
    public void evaluate() {
        AndTest test = new AndTest(Arrays.<Test>asList(
                new ContainsTest(new TranslatableText("upon")),
                new StartsWithTest(new TranslatableText("once"))
        ));
        assertTest(test, "Once upon a time", true, "upon Once");
        assertTest(test, "Once a time", false, null);
        assertTest(test, "upon this rock I once", false, null);
    }
}
