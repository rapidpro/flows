package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link NotEmptyTest}
 */
public class NotEmptyTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "not_empty");
        NotEmptyTest test = (NotEmptyTest) Test.fromJson(elm, m_deserializationContext);

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        NotEmptyTest test = new NotEmptyTest();
        assertTest(test, " ok  ", true, "ok");
        assertTest(test, "  ", false, null);
        assertTest(test, "", false, null);
    }
}
