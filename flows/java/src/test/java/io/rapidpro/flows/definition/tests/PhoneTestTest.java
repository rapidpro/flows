package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.tests.text.ContainsTest;
import org.junit.Test;

/**
 * Test for {@link ContainsTest}
 */
public class PhoneTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        PhoneTest.fromJson(parseObject("{}"), getDeserializationContext());
    }

    @Test
    public void evaluate() {
        PhoneTest test = new PhoneTest();

        assertTest(test, "My phone number is 0788 383 383", true, "+250788383383");
        assertTest(test, "+250788123123", true, "+250788123123");
        assertTest(test, "+12067799294", true, "+12067799294");

        assertTest(test, "My phone is 0124515", false, null);
    }
}
