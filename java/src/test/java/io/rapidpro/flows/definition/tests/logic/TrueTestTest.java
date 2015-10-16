package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link TrueTest}
 */
public class TrueTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "true");
        TrueTest test = (TrueTest) Test.fromJson(elm, m_deserializationContext);

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        TrueTest test = new TrueTest();
        assertTest(test, "huh?", true, "huh?");
    }
}
