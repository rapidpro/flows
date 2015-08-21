package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.runner.RunState;

import java.util.HashMap;
import java.util.Map;

/**
 * A test which can be evaluated to true or false on a given string
 */
public abstract class Test {

    protected static Map<String, Class<? extends Test>> s_classByType = new HashMap<>();
    static {
        s_classByType.put("true", TrueTest.class);
        s_classByType.put("false", FalseTest.class);
        s_classByType.put("contains", ContainsTest.class);
        s_classByType.put("contains_any", ContainsAnyTest.class);
        s_classByType.put("starts", StartsWithTest.class);
    }

    /**
     * Evaluates this test
     * @param run the run state
     * @param context the evaluation context
     * @param text the text to test against
     * @return the test result (true or false, and the matched portion of the input)
     */
    public abstract Result evaluate(RunState run, EvaluationContext context, String text);

    /**
     * Loads a test from the given JSON object
     */
    public static Test fromJson(JsonObject json) throws JsonSyntaxException {
        String type = json.get("type").getAsString();
        Class<? extends Test> clazz = s_classByType.get(type);
        return FlowUtils.fromJson(json, clazz);
    }

    /**
     * Holds the result of a test evaluation (the int value + the text matched)
     */
    public static class Result {
        public static Result NO_MATCH = new Result(false, null);

        protected boolean m_matched;
        protected String m_text;

        public Result(boolean matched, String text) {
            m_matched = matched;
            m_text = text;
        }

        public static Result textMatch(String text) {
            return new Result(true, text);
        }

        public boolean isMatched() {
            return m_matched;
        }

        public String getText() {
            return m_text;
        }

        @Override
        public String toString() {
            return "Test.Result{matches=" + m_matched + ", match=" + (m_text != null ? "\"" + m_text + "\"" : "null") + '}';
        }
    }

    /**
     * Base class for tests that have a translatable text argument
     */
    protected static abstract class Translatable extends Test {
        protected TranslatableText m_test;

        public Translatable(TranslatableText test) {
            m_test = test;
        }
    }
}
