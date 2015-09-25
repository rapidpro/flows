package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.runner.Runner;
import org.threeten.bp.LocalDate;

/**
 * Abstract base class for date tests which compare the input against a value
 */
public abstract class DateComparisonTest extends DateTest {

    protected String m_test;

    public DateComparisonTest(String test) {
        m_test = test;
    }

    /**
     * @see DateTest#evaluateAgainstDate(Runner, EvaluationContext, LocalDate)
     */
    @Override
    protected boolean evaluateAgainstDate(Runner runner, EvaluationContext context, LocalDate input) {
        EvaluatedTemplate test = runner.substituteVariables(m_test, context);

        if (!test.hasErrors()) {
            try {
                LocalDate testVal = Conversions.toDate(test.getOutput(), context);

                return doComparison(input, testVal);
            }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    protected abstract boolean doComparison(LocalDate input, LocalDate test);
}
