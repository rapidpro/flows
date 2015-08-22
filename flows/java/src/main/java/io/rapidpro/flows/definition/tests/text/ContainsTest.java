package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.TranslatableTest;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Test that returns whether the text contains the given words
 */
public class ContainsTest extends TranslatableTest {

    public ContainsTest(TranslatableText test) {
        super(test);
    }

    public static ContainsTest fromJson(JsonObject obj) throws FlowParseException {
        return new ContainsTest(TranslatableText.fromJson(obj.get("test")));
    }

    protected String testInWords(String test, String[] words, String[] rawWords) {
        for (int w = 0; w < words.length; w++) {
            String word = words[w];
            if (word.equals(test)) {
                return rawWords[w];
            }

            // words are over 4 characters and start with the same letter
            if (word.length() > 4 && test.length() > 4 && word.charAt(0) == test.charAt(0)) {
                // edit distance of 1 or less is a match
                if (FlowUtils.editDistance(word, test) <= 1) {
                    return rawWords[w];
                }
            }
        }
        return null;
    }

    /**
     * @see TranslatableTest#evaluateAgainstLocalized(RunState, EvaluationContext, String, String)
     */
    @Override
    protected Result evaluateAgainstLocalized(RunState run, EvaluationContext context, String text, String localizedTest) {
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

        // we are a match only if every test matches
        if (matches.size() == tests.length) {
            return Result.textMatch(StringUtils.join(matches, " "));
        } else {
            return Result.NO_MATCH;
        }
    }
}
