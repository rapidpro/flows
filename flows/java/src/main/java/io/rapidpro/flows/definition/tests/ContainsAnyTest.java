package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
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

    public static ContainsAnyTest fromJson(JsonObject json) {
        return new ContainsAnyTest(TranslatableText.fromJson(json.get("test")));
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        // localize and substitute any variables
        String localizedTest = m_test.getLocalized(run);
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
            return Result.textMatch(StringUtils.join(matches, " "));
        } else {
            return Result.NO_MATCH;
        }
    }
}
