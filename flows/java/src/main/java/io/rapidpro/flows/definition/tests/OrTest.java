package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.runner.RunState;

import java.util.Collection;

/**
 * Test which returns the OR'ed result of other tests
 */
public class OrTest extends Test {

    protected Collection<Test> m_tests;

    public OrTest(Collection<Test> tests) {
        m_tests = tests;
    }

    public static OrTest fromJson(JsonObject obj) throws FlowParseException {
        return new OrTest(Test.fromJsonArray(obj.get("tests").getAsJsonArray()));
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        for (Test test : m_tests) {
            Result result = test.evaluate(run, context, text);
            if (result.isMatched()) {
                return result;
            }
        }
        return Result.NO_MATCH;
    }
}
