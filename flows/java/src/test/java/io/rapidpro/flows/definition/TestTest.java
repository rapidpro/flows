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
        s_context = s_run.buildContext();
    }

    @org.junit.Test
    public void _true() {
        Test test = new Test.True();
        assertThat(test.evaluate(s_run, s_context, "huh?"), is(new Test.Result(1, "huh?")));
    }

    @org.junit.Test
    public void _false() {
        Test test = new Test.False();
        assertThat(test.evaluate(s_run, s_context, "huh?"), is(new Test.Result(0, "huh?")));
    }

    @org.junit.Test
    public void contains() {
        Test test = new Test.Contains(new TranslatableText("north,east"));

        assertThat(test.evaluate(s_run, s_context, "go north east"), is(new Test.Result(2, "north east")));
        assertThat(test.evaluate(s_run, s_context, "EAST then NORTH"), is(new Test.Result(2, "NORTH EAST")));

        assertThat(test.evaluate(s_run, s_context, "go north"), is(Test.Result.NO_MATCH));
        assertThat(test.evaluate(s_run, s_context, "east"), is(Test.Result.NO_MATCH));
    }

    @org.junit.Test
    public void containsAny() {
        Test test = new Test.ContainsAny(new TranslatableText("yes,yeah"));

        assertThat(test.evaluate(s_run, s_context, "yes"), is(new Test.Result(1, "yes")));
        assertThat(test.evaluate(s_run, s_context, "Ok YES I will"), is(new Test.Result(1, "YES")));
        assertThat(test.evaluate(s_run, s_context, "yeah sure"), is(new Test.Result(1, "yeah")));

        assertThat(test.evaluate(s_run, s_context, "no"), is(Test.Result.NO_MATCH));
        assertThat(test.evaluate(s_run, s_context, "NO way jose"), is(Test.Result.NO_MATCH));
    }
}
