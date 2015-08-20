package io.rapidpro.flows.definition;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.runner.Contact;
import io.rapidpro.flows.runner.ContactUrn;
import io.rapidpro.flows.runner.Org;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Test} and its subclasses
 */
public class TestTest {

    private static RunState s_run;
    private static EvaluationContext s_context;

    @BeforeClass
    public static void setup() throws Exception {
        String flowJson = IOUtils.toString(TestTest.class.getClassLoader().getResourceAsStream("flows/mushrooms.json"));

        Org org = new Org("eng", ZoneId.of("Africa/Kigali"), true, false);
        Contact contact = new Contact("1234-1234", "Joe", Arrays.asList(ContactUrn.parse("tel:+260964153686")), Collections.singleton("Testers"), new HashMap<String, String>(), "eng");
        Flow flow = Flow.fromJson(flowJson);

        Flows.Runner runner = Flows.getRunner();
        s_run = runner.start(org, contact, flow);
        s_context = s_run.buildContext(null);
    }

    @org.junit.Test
    public void _true() {
        Test test = new Test.True();
        assertTest(test, "huh?", true, "huh?");
    }

    @org.junit.Test
    public void _false() {
        Test test = new Test.False();
        assertTest(test, "huh?", false, "huh?");
    }

    @org.junit.Test
    public void contains() {
        Test test = new Test.Contains(new TranslatableText("north,east"));

        assertTest(test, "go north east", true, "north east");
        assertTest(test, "EAST then NORRTH", true, "NORRTH EAST");

        assertTest(test, "go north", false, null);
        assertTest(test, "east", false, null);
    }

    @org.junit.Test
    public void containsAny() {
        Test test = new Test.ContainsAny(new TranslatableText("yes,affirmative"));

        assertTest(test, "yes", true, "yes");
        assertTest(test, "AFFIRMATIVE SIR", true, "AFFIRMATIVE");
        assertTest(test, "affirmative yes", true, "yes affirmative");
        assertTest(test, "afirmative!", true, "afirmative"); // edit distance

        // edit distance doesn't apply for words shorter than 4 chars
        assertTest(test, "Ok YEES I will", false, null);

        assertTest(test, "no", false, null);
        assertTest(test, "NO way jose", false, null);
    }

    @org.junit.Test
    public void startsWith() {
        Test test = new Test.StartsWith(new TranslatableText("once"));

        assertTest(test, "ONCE", true, "ONCE");
        assertTest(test, "Once upon a time", true, "Once");

        assertTest(test, "Hey once", false, null);
    }

    protected void assertTest(Test test, String input, boolean expectedMatched, String expectedText) {
        Test.Result result = test.evaluate(s_run, s_context, input);
        assertThat(result.isMatched(), is(expectedMatched));
        assertThat(result.getText(), is(expectedText));
    }
}
