package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link FalseTest}
 */
public class FalseTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "false");
        FalseTest test = (FalseTest) Test.fromJson(obj, m_deserializationContext);

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        FalseTest test = new FalseTest();
        assertTest(test, "huh?", false, "huh?");
    }
}
