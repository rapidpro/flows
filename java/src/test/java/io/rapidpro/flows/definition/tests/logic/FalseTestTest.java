package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonElement;
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
        JsonElement elm = JsonUtils.object("type", "false");
        FalseTest test = (FalseTest) Test.fromJson(elm, m_deserializationContext);

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        FalseTest test = new FalseTest();
        assertTest(test, "huh?", false, "huh?");
    }
}
