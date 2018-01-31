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
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test that returns whether the text contains the given words
 */
public class ContainsTest extends TranslatableTest {

    public static final String TYPE = "contains";

    public ContainsTest(TranslatableText test) {
        super(test);
    }

    /**
     * @see Test#fromJson(JsonElement, Flow.DeserializationContext)
     */
    public static ContainsTest fromJson(JsonElement elm, Flow.DeserializationContext context) throws FlowParseException {
        JsonObject obj = elm.getAsJsonObject();
        return new ContainsTest(TranslatableText.fromJson(obj.get("test")));
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("type", TYPE, "test", m_test.toJson());
    }

    protected boolean findMatches(SortedSet<Integer> matches, String test, String[] words, String[] rawWords) {
        boolean matched = false;
        for (int w = 0; w < words.length; w++) {
            String word = words[w];
            if (word.equals(test)) {
                matches.add(w);
                matched = true;
                continue;
            }
        }
        return matched;
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
        SortedSet<Integer> matches = new TreeSet<>();
        int matchCount = 0;
        for (String test : tests) {
            boolean matched = findMatches(matches, test, words, rawWords);
            if (matched){
                matchCount += 1;
            }
        }

        // we are a match only if every test matches
        if (matchCount == tests.length) {
            // build our actual matches as strings
            ArrayList<String> matchingWords = new ArrayList<>();
            for (int matchIndex: matches){
                matchingWords.add(rawWords[matchIndex]);
            }

            return Result.match(StringUtils.join(matchingWords, " "));
        } else {
            return Result.NO_MATCH;
        }
    }
}
