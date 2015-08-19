package io.rapidpro.flows.definition;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class Test {

    protected static Map<String, Class<? extends Test>> s_classByType = new HashMap<>();
    static {
        s_classByType.put("true", True.class);
        s_classByType.put("false", False.class);
        s_classByType.put("contains", Contains.class);
        s_classByType.put("contains_any", ContainsAny.class);
    }

    protected TranslatableText m_test;

    public abstract Result evaluate(RunState run, EvaluationContext context, String text);

    /**
     * Loads a test from the given JSON object
     */
    public static Test fromJson(JsonObject json) throws JsonSyntaxException {
        String type = json.get("type").getAsString();
        Class<? extends Test> clazz = s_classByType.get(type);
        Test test = FlowUtils.fromJson(json, clazz);

        test.m_test = TranslatableText.fromJson(json.get("test"));

        return test;
    }

    /**
     * Holds the result of a test evaluation (the int value + the text matched)
     */
    public static class Result {
        public static Result NEGATIVE = new Result(0, null);

        protected Integer m_value;
        protected String m_match;

        public Result(Integer value, String match) {
            m_value = value;
            m_match = match;
        }

        public Integer getValue() {
            return m_value;
        }

        public String getMatch() {
            return m_match;
        }
    }

    /**
     * Test that always returns true
     */
    public static class True extends Test {
        public static True fromJson(JsonObject json) {
            return new True();
        }

        @Override
        public Result evaluate(RunState run, EvaluationContext context, String text) {
            return new Result(1, text);
        }
    }

    /**
     * Test that always returns false
     */
    public static class False extends Test {
        public static False fromJson(JsonObject json) {
            return new False();
        }

        @Override
        public Result evaluate(RunState run, EvaluationContext context, String text) {
            return new Result(0, text);
        }
    }

    /**
     * Test that returns whether the text contains the given words
     */
    public static class Contains extends Test {
        public static Contains fromJson(JsonObject json) {
            return new Contains();
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

        @Override
        public Result evaluate(RunState run, EvaluationContext context, String text) {
            throw new UnknownError();
        }
    }

    /**
     * Test that returns whether the text contains any of the given words
     */
    public static class ContainsAny extends Contains {
        public static ContainsAny fromJson(JsonObject json) {
            return new ContainsAny();
        }

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

            // we are a match only if every test matches
            if (matches.size() == tests.length) {
                return new Result(tests.length, StringUtils.join(matches, " "));
            } else {
                return Result.NEGATIVE;
            }
        }
    }
}
