package io.rapidpro.flows.definition.tests.location;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

/**
 * Test for {@link HasStateTest}
 */
public class HasStateTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasStateTest.fromJson(parseObject("{}"), m_deserializationContext);
    }

    @Test
    public void evaluate() {
        HasStateTest test = new HasStateTest();

        assertTest(test, " kigali", true, "Kigali");
        assertTest(test, "Washington", false, null);
    }
}
