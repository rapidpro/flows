package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.Runner;

import java.math.BigDecimal;

/**
 * Abstract base class for numeric tests which compare the input against a value
 */
public abstract class NumericComparisonTest extends NumericTest {

    protected String m_test;

    public NumericComparisonTest(String test) {
        m_test = test;
    }

    /**
     * @see NumericTest#evaluateAgainstDecimal(Runner, EvaluationContext, BigDecimal)
     */
    @Override
    protected boolean evaluateAgainstDecimal(Runner runner, EvaluationContext context, BigDecimal input) {
        EvaluatedTemplate test = runner.substituteVariables(m_test, context);

        if (!test.hasErrors()) {
            try {
                BigDecimal testVal = new BigDecimal(test.getOutput().trim());

                return doComparison(input, testVal);
            }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    protected abstract boolean doComparison(BigDecimal input, BigDecimal test);
}
