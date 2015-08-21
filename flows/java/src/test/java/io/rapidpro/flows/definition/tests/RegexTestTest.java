package io.rapidpro.flows.definition.tests;

import io.rapidpro.flows.definition.TranslatableText;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ContainsTest}
 */
public class RegexTestTest extends BaseTestTest {
    @Test
    public void evaluate() {
        RegexTest test = new RegexTest(new TranslatableText("(?P<first_name>\\w+) (\\w+)"));

        assertTest(test, "Isaac Newton", true, "Isaac Newton");
        assertTest(test, "Isaac", false, null);

        assertThat(m_run.getExtra(), hasEntry("0", "Isaac Newton"));
        assertThat(m_run.getExtra(), hasEntry("1", "Isaac"));
        assertThat(m_run.getExtra(), hasEntry("2", "Newton"));
        assertThat(m_run.getExtra(), hasEntry("first_name", "Isaac"));
    }

    @Test
    public void pythonToJavaRegex() {
        Map<String, String> groupNames = new HashMap<>();
        String regex = RegexTest.pythonToJavaRegex("(?P<first_name>\\w+) (\\w+)", groupNames);

        assertThat(regex, is("(?<name1>\\w+) (\\w+)"));
        assertThat(groupNames, hasEntry("name1", "first_name"));
    }
}
