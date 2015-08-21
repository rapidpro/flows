package io.rapidpro.flows.definition.tests;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.FlowParseException;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test which returns the AND'ed result of other tests
 */
public class AndTest extends Test {

    protected Collection<Test> m_tests;

    public AndTest(Collection<Test> tests) {
        m_tests = tests;
    }

    public static AndTest fromJson(JsonObject obj) throws FlowParseException {
        return new AndTest(Test.fromJsonArray(obj.get("tests").getAsJsonArray()));
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        List<String> matches = new ArrayList<>();
        for (Test test : m_tests) {
            Test.Result result = test.evaluate(run, context, text);
            if (result.isMatched()) {
                matches.add(result.getText());
            } else {
                return Result.NO_MATCH;
            }
        }
        return Test.Result.textMatch(StringUtils.join(matches, " "));
    }
}
