package io.rapidpro.flows.definition.tests.location;

import io.rapidpro.flows.definition.tests.BaseTestTest;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasDistrictTest}
 */
public class HasDistrictTestTest extends BaseTestTest {

    @Test
    public void fromJson() throws Exception {
        HasDistrictTest test = HasDistrictTest.fromJson(parseObject("{\"test\": \"kigali\"}"), m_deserializationContext);
        assertThat(test.getState(), is("kigali"));
    }

    @Test
    public void evaluate() {
        HasDistrictTest test = new HasDistrictTest("kigali");

        assertTest(test, " gasabo", true, "Gasabo");
        assertTest(test, "Nine", false, null);

        ((Map<String, String>) m_context.getVariables().get("extra")).put("homestate", "Kigali");
        test = new HasDistrictTest("@extra.homestate");

        assertTest(test, " gasabo", true, "Gasabo");
        assertTest(test, "Nine", false, null);
    }
}
