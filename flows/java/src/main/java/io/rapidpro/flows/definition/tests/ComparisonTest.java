package io.rapidpro.flows.definition.tests;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.RunState;

import java.math.BigDecimal;

/**
 * Abstract base class for numeric tests which compare the input against a value
 */
public abstract class ComparisonTest extends NumericTest {

    protected String m_test;

    public ComparisonTest(String test) {
        m_test = test;
    }

    /**
     * @see NumericTest#evaluateAgainstDecimal(RunState, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateAgainstDecimal(RunState run, EvaluationContext context, BigDecimal decimal) {
        EvaluatedTemplate test = run.substituteVariables(m_test, context);

        if (!test.hasErrors()) {
            try {
                BigDecimal testVal = new BigDecimal(test.getOutput().trim());

                return doComparison(decimal, testVal);
            }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    protected abstract boolean doComparison(BigDecimal input, BigDecimal test);
}
