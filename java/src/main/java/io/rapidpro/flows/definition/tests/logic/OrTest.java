package io.rapidpro.flows.definition.tests.logic;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.util.Collection;

/**
 * Test which returns the OR'ed result of other tests
 */
public class OrTest extends Test {

    public static final String TYPE = "or";

    protected Collection<Test> m_tests;

    public OrTest(Collection<Test> tests) {
        m_tests = tests;
    }

    /**
     * @see Test#fromJson(JsonObject, Flow.DeserializationContext)
     */
    public static OrTest fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        return new OrTest(Test.fromJsonArray(obj.get("tests").getAsJsonArray(), context));
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        for (Test test : m_tests) {
            Result result = test.evaluate(runner, run, context, text);
            if (result.isMatched()) {
                return result;
            }
        }
        return Result.NO_MATCH;
    }
}
