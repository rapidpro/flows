package io.rapidpro.flows.definition.tests.text;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

/**
 * Abstract base class for tests that have a translatable text argument
 */
public abstract class TranslatableTest extends Test {

    protected TranslatableText m_test;

    protected TranslatableTest(TranslatableText test) {
        m_test = test;
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        String localizedTest = m_test.getLocalized(run);

        return evaluateAgainstLocalized(runner, run, context, text, localizedTest);
    }

    /**
     * Evaluates the test against the given localized text value. Subclasses must implement this.
     * @param runner the flow runner
     * @param run the current run state
     * @param context the evaluation context
     * @param text the input
     * @param localizedTest the localized test value
     * @return the test result
     */
    protected abstract Result evaluateAgainstLocalized(Runner runner, RunState run, EvaluationContext context, String text, String localizedTest);

    public TranslatableText getTest() {
        return m_test;
    }
}
