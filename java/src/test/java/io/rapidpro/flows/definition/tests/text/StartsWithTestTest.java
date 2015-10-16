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
 * Test for {@link StartsWithTest}
 */
public class StartsWithTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "starts", "test", "once");
        StartsWithTest test = (StartsWithTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_test, is(new TranslatableText("once")));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        StartsWithTest test = new StartsWithTest(new TranslatableText("once"));

        assertTest(test, "  ONCE", true, "ONCE");
        assertTest(test, "Once upon a time", true, "Once");

        assertTest(test, "Hey once", false, null);
    }
}
