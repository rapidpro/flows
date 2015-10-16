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
 * Test for {@link AndTest}
 */
public class AndTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonObject obj = JsonUtils.object("type", "and", "tests", JsonUtils.array(
                JsonUtils.object("type", "contains", "test", "upon"),
                JsonUtils.object("type", "starts", "test", "once")
        ));
        AndTest test = (AndTest) Test.fromJson(obj, m_deserializationContext);
        assertThat(test.m_tests, hasSize(2));
        assertThat(test.m_tests.get(0), instanceOf(ContainsTest.class));
        assertThat(test.m_tests.get(1), instanceOf(StartsWithTest.class));

        assertThat(test.toJson(), is((JsonElement) obj));
    }

    @org.junit.Test
    public void evaluate() {
        AndTest test = new AndTest(Arrays.<Test>asList(
                new ContainsTest(new TranslatableText("upon")),
                new StartsWithTest(new TranslatableText("once"))
        ));
        assertTest(test, "Once upon a time", true, "upon Once");
        assertTest(test, "Once a time", false, null);
        assertTest(test, "upon this rock I once", false, null);
    }
}
