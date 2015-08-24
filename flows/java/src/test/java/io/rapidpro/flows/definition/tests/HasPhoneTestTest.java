package io.rapidpro.flows.definition.tests;

import org.junit.Test;

/**
 * Test for {@link HasPhoneTest}
 */
public class HasPhoneTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasPhoneTest.fromJson(parseObject("{}"), getDeserializationContext());
    }

    @Test
    public void evaluate() {
        HasPhoneTest test = new HasPhoneTest();

        assertTest(test, "My phone number is 0788 383 383", true, "+250788383383");
        assertTest(test, "+250788123123", true, "+250788123123");
        assertTest(test, "+12067799294", true, "+12067799294");

        assertTest(test, "My phone is 0124515", false, null);
    }
}
