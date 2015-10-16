package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.BaseTestTest;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ContainsTest}
 */
public class RegexTestTest extends BaseTestTest {

    @org.junit.Test
    public void toAndFromJson() throws Exception {
        JsonElement elm = JsonUtils.object("type", "regex", "test", "(?P<first_name>\\w+) (\\w+)");
        RegexTest test = (RegexTest) Test.fromJson(elm, m_deserializationContext);
        assertThat(test.getTest(), is(new TranslatableText("(?P<first_name>\\w+) (\\w+)")));

        assertThat(test.toJson(), is(elm));
    }

    @org.junit.Test
    public void evaluate() {
        RegexTest test = new RegexTest(new TranslatableText("(?P<first_name>\\w+) (\\w+)"));

        assertTest(test, "Isaac Newton", true, "Isaac Newton");
        assertTest(test, "Isaac", false, null);

        assertThat(m_run.getExtra(), hasEntry("0", (Object) "Isaac Newton"));
        assertThat(m_run.getExtra(), hasEntry("1", (Object) "Isaac"));
        assertThat(m_run.getExtra(), hasEntry("2", (Object) "Newton"));
        assertThat(m_run.getExtra(), hasEntry("first_name", (Object) "Isaac"));
    }

    @org.junit.Test
    public void pythonToJavaRegex() {
        Map<String, String> groupNames = new HashMap<>();
        String regex = RegexTest.pythonToJavaRegex("(?P<first_name>\\w+) (\\w+)", groupNames);

        assertThat(regex, is("(?<name1>\\w+) (\\w+)"));
        assertThat(groupNames, hasEntry("name1", "first_name"));
    }
}
