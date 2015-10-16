package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ContainsTest}
 */
public class ContainsTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "contains", "test", "north,east");
        ContainsTest test = (ContainsTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is(new TranslatableText("north,east")));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        ContainsTest test = new ContainsTest(new TranslatableText("north,east"));

        assertTest(test, "go north east", true, "north east");
        assertTest(test, "EAST then NORRTH", true, "NORRTH EAST");

        assertTest(test, "go north", false, null);
        assertTest(test, "east", false, null);
    }
}
