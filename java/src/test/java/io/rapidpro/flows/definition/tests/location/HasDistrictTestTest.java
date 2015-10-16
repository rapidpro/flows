package io.rapidpro.flows.definition.tests.location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasDistrictTest}
 */
public class HasDistrictTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "district", "test", "Kigali");
        HasDistrictTest test = (HasDistrictTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_state, is("Kigali"));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
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
