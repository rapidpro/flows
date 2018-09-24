package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonElement;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasPhoneTest}
 */
public class HasPhoneTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "phone");
        HasPhoneTest test = (HasPhoneTest) Test.fromJson(elm, m_deserializationContext);

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        HasPhoneTest test = new HasPhoneTest();

        assertTest(test, "My phone number is 0788 383 383", true, "+250788383383");
        assertTest(test, "+250788123123", true, "+250788123123");
        assertTest(test, "+12067799294", true, "+12067799294");
        assertTest(test, "+92 310 1234567", true, "+923101234567");

        assertTest(test, "My phone is 0124515", false, null);
    }
}
