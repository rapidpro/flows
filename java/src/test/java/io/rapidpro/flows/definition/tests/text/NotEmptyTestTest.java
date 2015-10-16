package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        JsonObject obj = JsonUtils.object("type", "not_empty");
        NotEmptyTest test = (NotEmptyTest) Test.fromJson(obj, m_deserializationContext);

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        NotEmptyTest test = new NotEmptyTest();
        assertTest(test, " ok  ", true, "ok");
        assertTest(test, "  ", false, null);
        assertTest(test, "", false, null);
    }
}
