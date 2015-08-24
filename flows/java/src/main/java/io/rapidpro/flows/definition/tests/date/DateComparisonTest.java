package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.runner.RunState;

import java.time.LocalDate;

/**
 * Abstract base class for date tests which compare the input against a value
 */
public abstract class DateComparisonTest extends DateTest {

    protected String m_test;

    public DateComparisonTest(String test) {
        m_test = test;
    }

    /**
     * @see DateTest#evaluateAgainstDate(RunState, EvaluationContext, LocalDate)
     */
    @Override
    protected boolean evaluateAgainstDate(RunState run, EvaluationContext context, LocalDate input) {
        EvaluatedTemplate test = run.substituteVariables(m_test, context);

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
