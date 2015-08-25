package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.date.DateAfterTest;
import io.rapidpro.flows.definition.tests.date.DateBeforeTest;
import io.rapidpro.flows.definition.tests.date.DateEqualTest;
import io.rapidpro.flows.definition.tests.date.HasDateTest;
import io.rapidpro.flows.definition.tests.location.HasDistrictTest;
import io.rapidpro.flows.definition.tests.location.HasStateTest;
import io.rapidpro.flows.definition.tests.logic.AndTest;
import io.rapidpro.flows.definition.tests.logic.FalseTest;
import io.rapidpro.flows.definition.tests.logic.OrTest;
import io.rapidpro.flows.definition.tests.logic.TrueTest;
import io.rapidpro.flows.definition.tests.numeric.*;
import io.rapidpro.flows.definition.tests.text.*;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A test which can be evaluated to true or false on a given string
 */
public abstract class Test {

    protected static Map<String, Class<? extends Test>> s_classByType = new HashMap<>();
    static {
        s_classByType.put("true", TrueTest.class);
        s_classByType.put("false", FalseTest.class);
        s_classByType.put("and", AndTest.class);
        s_classByType.put("or", OrTest.class);
        s_classByType.put("not_empty", NotEmptyTest.class);
        s_classByType.put("contains", ContainsTest.class);
        s_classByType.put("contains_any", ContainsAnyTest.class);
        s_classByType.put("starts", StartsWithTest.class);
        s_classByType.put("regex", RegexTest.class);
        s_classByType.put("number", HasNumberTest.class);
        s_classByType.put("between", BetweenTest.class);
        s_classByType.put("eq", EqualTest.class);
        s_classByType.put("lt", LessThanTest.class);
        s_classByType.put("lte", LessThanOrEqualTest.class);
        s_classByType.put("gt", GreaterThanTest.class);
        s_classByType.put("gte", GreaterThanOrEqualTest.class);
        s_classByType.put("date", HasDateTest.class);
        s_classByType.put("date_equal", DateEqualTest.class);
        s_classByType.put("date_before", DateBeforeTest.class);
        s_classByType.put("date_after", DateAfterTest.class);
        s_classByType.put("phone", HasPhoneTest.class);
        s_classByType.put("state", HasStateTest.class);
        s_classByType.put("district", HasDistrictTest.class);
    }

    /**
     * Creates a test from the given JSON object
     * @param obj the JSON object
     * @param context the deserialization context
     * @return the test
     */
    public static Test fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        String type = obj.get("type").getAsString();
        Class<? extends Test> clazz = s_classByType.get(type);
        if (clazz == null) {
            throw new FlowParseException("Unknown test type: " + type);
        }

        return JsonUtils.fromJson(obj, context, clazz);
    }

    /**
     * Creates a list of tests from the given JSON array
     * @param array the JSON array
     * @param context the deserialization context
     * @return the tests
     */
    public static List<Test> fromJsonArray(JsonArray array, Flow.DeserializationContext context) throws FlowParseException {
        List<Test> tests = new ArrayList<>();
        for (JsonElement testElem : array) {
            tests.add(Test.fromJson(testElem.getAsJsonObject(), context));
        }
        return tests;
    }

    /**
     * Evaluates this test
     * @param runner the flow runner
     * @param run the current run state
     * @param context the evaluation context
     * @param text the text to test against
     * @return the test result (true or false, and the matched portion of the input)
     */
    public abstract Result evaluate(Flows.Runner runner, RunState run, EvaluationContext context, String text);

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
}
