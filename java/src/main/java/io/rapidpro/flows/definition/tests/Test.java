package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
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
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.threeten.bp.LocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A test which can be evaluated to true or false on a given string
 */
public abstract class Test implements Jsonizable {

    protected static Map<String, Class<? extends Test>> s_classByType = new HashMap<>();
    static {
        s_classByType.put(TrueTest.TYPE, TrueTest.class);
        s_classByType.put(FalseTest.TYPE, FalseTest.class);
        s_classByType.put(AndTest.TYPE, AndTest.class);
        s_classByType.put(OrTest.TYPE, OrTest.class);
        s_classByType.put(NotEmptyTest.TYPE, NotEmptyTest.class);
        s_classByType.put(ContainsTest.TYPE, ContainsTest.class);
        s_classByType.put(ContainsAnyTest.TYPE, ContainsAnyTest.class);
        s_classByType.put(StartsWithTest.TYPE, StartsWithTest.class);
        s_classByType.put(RegexTest.TYPE, RegexTest.class);
        s_classByType.put(HasNumberTest.TYPE, HasNumberTest.class);
        s_classByType.put(EqualTest.TYPE, EqualTest.class);
        s_classByType.put(LessThanTest.TYPE, LessThanTest.class);
        s_classByType.put(LessThanOrEqualTest.TYPE, LessThanOrEqualTest.class);
        s_classByType.put(GreaterThanTest.TYPE, GreaterThanTest.class);
        s_classByType.put(GreaterThanOrEqualTest.TYPE, GreaterThanOrEqualTest.class);
        s_classByType.put(BetweenTest.TYPE, BetweenTest.class);
        s_classByType.put(HasDateTest.TYPE, HasDateTest.class);
        s_classByType.put(DateEqualTest.TYPE, DateEqualTest.class);
        s_classByType.put(DateBeforeTest.TYPE, DateBeforeTest.class);
        s_classByType.put(DateAfterTest.TYPE, DateAfterTest.class);
        s_classByType.put(HasPhoneTest.TYPE, HasPhoneTest.class);
        s_classByType.put(HasStateTest.TYPE, HasStateTest.class);
        s_classByType.put(HasDistrictTest.TYPE, HasDistrictTest.class);
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
    public abstract Result evaluate(Runner runner, RunState run, EvaluationContext context, String text);

    /**
     * Holds the result of a test evaluation (matched + the value matched)
     */
    public static class Result {
        public static Result NO_MATCH = new Result(false, null);

        protected boolean m_matched;

        protected Object m_value;

        public Result(boolean matched, Object value) {
            m_matched = matched;
            m_value = value;
        }

        public static Result match(String text) {
            return new Result(true, text);
        }

        public static Result match(BigDecimal value) {
            return new Result(true, value);
        }

        public static Result match(LocalDate value) {
            return new Result(true, value);
        }

        public boolean isMatched() {
            return m_matched;
        }

        public Object getValue() {
            return m_value;
        }

        @Override
        public String toString() {
            return "Test.Result{matched=" + m_matched + ", value=" + m_value + '}';
        }
    }
}
