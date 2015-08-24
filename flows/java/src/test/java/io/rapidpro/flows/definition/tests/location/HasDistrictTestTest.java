package io.rapidpro.flows.definition.tests.location;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasDistrictTest}
 */
public class HasDistrictTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasDistrictTest test = HasDistrictTest.fromJson(parseObject("{\"test\": \"kigali\"}"), getDeserializationContext());
        assertThat(test.getState(), is("kigali"));
    }

    @Test
    public void evaluate() {
        HasDistrictTest test = new HasDistrictTest("kigali");

        assertTest(test, " gasabo", true, "Gasabo");
        assertTest(test, "Nine", false, null);

        getContext().putVariable("homestate", "Kigali");
        test = new HasDistrictTest("@homestate");

        assertTest(test, " gasabo", true, "Gasabo");
        assertTest(test, "Nine", false, null);
    }
}
