package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Test that returns whether the text contains any of the given words
 */
public class ContainsAnyTest extends ContainsTest {

    public ContainsAnyTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static ContainsAnyTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new ContainsAnyTest(TranslatableText.fromJson(obj.get("test")));
    }

    /**
     * @see TranslatableTest#evaluateAgainstLocalized(RunState, EvaluationContext, String, String)
     */
    @Override
    protected Test.Result evaluateAgainstLocalized(RunState run, EvaluationContext context, String text, String localizedTest) {
        localizedTest = run.substituteVariables(localizedTest, context).getOutput();

        // tokenize our test
        Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
        String[] tests = pattern.split(localizedTest.toLowerCase());

        // tokenize our input
        String[] words = pattern.split(text.toLowerCase());
        String[] rawWords = pattern.split(text);

        // run through each of our tests
        List<String> matches = new ArrayList<>();
        for (String test : tests) {
            String match = testInWords(test, words, rawWords);
            if (StringUtils.isNotEmpty(match)) {
                matches.add(match);
            }
        }

        // we are a match if at least one test matches
        if (matches.size() > 0) {
            return Test.Result.textMatch(StringUtils.join(matches, " "));
        } else {
            return Test.Result.NO_MATCH;
        }
    }
}
