package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.EvaluationError;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import org.threeten.bp.LocalDate;

/**
 * Abstract base class for tests that are date based
 */
public abstract class DateTest extends Test {

    /**
     * @see Test#evaluate(RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(RunState run, EvaluationContext context, String text) {
        // test every word in the message against our test

        try {
            LocalDate date = Conversions.toDate(text, context);
            if (evaluateAgainstDate(run, context, date)) {
                return Result.textMatch(Conversions.toString(date, context));
            }
        }
        catch (EvaluationError ignored) {}

        return Result.NO_MATCH;
    }

    /**
     * Evaluates the test against the given decimal value. Subclasses must implement this.
     * @param run the run state
     * @param context the evaluation context
     * @param date the date value
     * @return the test result
     */
    protected abstract boolean evaluateAgainstDate(RunState run, EvaluationContext context, LocalDate date);
}
