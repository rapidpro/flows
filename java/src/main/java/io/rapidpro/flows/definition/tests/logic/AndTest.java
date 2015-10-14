package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test which returns the AND'ed result of other tests
 */
public class AndTest extends Test {

    public static final String TYPE = "and";

    protected Collection<Test> m_tests;

    public AndTest(Collection<Test> tests) {
        m_tests = tests;
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static AndTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new AndTest(Test.fromJsonArray(obj.get("tests").getAsJsonArray(), context));
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        List<String> matches = new ArrayList<>();
        for (Test test : m_tests) {
            Test.Result result = test.evaluate(runner, run, context, text);
            if (result.isMatched()) {
                matches.add(Conversions.toString(result.getValue(), context));
            } else {
                return Result.NO_MATCH;
            }
        }
        return Test.Result.match(StringUtils.join(matches, " "));
    }
}
