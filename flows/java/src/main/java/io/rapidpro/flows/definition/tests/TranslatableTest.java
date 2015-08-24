package io.rapidpro.flows.definition.tests;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.TranslatableText;
import io.rapidpro.flows.runner.RunState;

/**
 * Abstract base class for tests that have a translatable text argument
 */
public abstract class TranslatableTest extends Test {

    protected TranslatableText m_test;

    protected TranslatableTest(TranslatableText test) {
        m_test = test;
    }

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        String localizedTest = m_test.getLocalized(run);

        return evaluateAgainstLocalized(run, context, text, localizedTest);
    }

    protected abstract Result evaluateAgainstLocalized(RunState run, EvaluationContext context, String text, String localizedTest);

    public TranslatableText getTest() {
        return m_test;
    }
}
