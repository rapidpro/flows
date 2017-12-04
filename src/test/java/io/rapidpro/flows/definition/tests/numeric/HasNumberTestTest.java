package io.rapidpro.flows.definition.tests.numeric;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link HasNumberTest}
 */
public class HasNumberTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "number");
        HasNumberTest test = (HasNumberTest) Test.fromJson(elm, m_deserializationContext);

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        HasNumberTest test = new HasNumberTest();

        assertTest(test, "32 cats", true, new BigDecimal(32));
        assertTest(test, "4l dogs", true, new BigDecimal(41));
        assertTest(test, "cats", false, null);
        assertTest(test, "dogs", false, null);
    }
}
