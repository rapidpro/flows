package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.definition.tests.text.ContainsTest;
import io.rapidpro.flows.definition.tests.text.StartsWithTest;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link OrTest}
 */
public class OrTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "or", "tests", JsonUtils.array(
                JsonUtils.object("type", "contains", "test", "upon"),
                JsonUtils.object("type", "starts", "test", "once")
        ));
        OrTest test = (OrTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_tests, hasSize(2));
        assertThat(test.m_tests.get(0), instanceOf(ContainsTest.class));
        assertThat(test.m_tests.get(1), instanceOf(StartsWithTest.class));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        OrTest test = new OrTest(Arrays.<Test>asList(
                new ContainsTest(new TranslatableText("upon")),
                new StartsWithTest(new TranslatableText("once"))
        ));
        assertTest(test, "Once upon a time", true, "upon");
        assertTest(test, "Once a time", true, "Once");
        assertTest(test, "upon this rock I once", true, "upon");
        assertTest(test, "huh", false, null);
    }
}
