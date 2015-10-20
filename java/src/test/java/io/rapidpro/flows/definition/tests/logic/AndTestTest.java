package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonElement;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.definition.tests.text.ContainsTest;
import io.rapidpro.flows.definition.tests.text.StartsWithTest;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AndTest}
 */
public class AndTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "and", "tests", JsonUtils.array(
                JsonUtils.object("type", "contains", "test", "upon"),
                JsonUtils.object("type", "starts", "test", "once")
        ));
        AndTest test = (AndTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTests(), hasSize(2));
        assertThat(test.getTests().get(0), instanceOf(ContainsTest.class));
        assertThat(test.getTests().get(1), instanceOf(StartsWithTest.class));

        assertThat(test.toJson(), is(elm));
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
