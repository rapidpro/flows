package io.rapidpro.flows.definition.tests.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.FlowUtils;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Test that returns whether the text contains the given words
 */
public class ContainsTest extends TranslatableTest {

    public static final String TYPE = "contains";

    public ContainsTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static ContainsTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new ContainsTest(TranslatableText.fromJson(obj.get("test")));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "test", m_test.toJson());
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
     * @see TranslatableTest#evaluateForLocalized(Runner, RunState, EvaluationContext, String, String)
     */
    @Override
    protected Result evaluateForLocalized(Runner runner, RunState run, EvaluationContext context, String text, String localizedTest) {
        localizedTest = runner.substituteVariables(localizedTest, context).getOutput();

        // tokenize our test
        String[] tests = ExpressionUtils.tokenize(localizedTest.toLowerCase());

        // tokenize our input
        String[] words = ExpressionUtils.tokenize(text.toLowerCase());
        String[] rawWords = ExpressionUtils.tokenize(text);

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
            return Result.match(StringUtils.join(matches, " "));
        } else {
            return Result.NO_MATCH;
        }
    }
}
