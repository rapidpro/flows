package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
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
        JsonElement elm = JsonUtils.object("type", "starts", "test", "once");
        StartsWithTest test = (StartsWithTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is(new TranslatableText("once")));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        StartsWithTest test = new StartsWithTest(new TranslatableText("once"));

        assertTest(test, "  ONCE", true, "ONCE");
        assertTest(test, "Once upon a time", true, "Once");

        assertTest(test, "Hey once", false, null);
    }
}
