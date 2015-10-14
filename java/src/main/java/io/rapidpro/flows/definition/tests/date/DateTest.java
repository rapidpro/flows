package io.rapidpro.flows.definition.tests.date;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.EvaluationError;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import org.threeten.bp.LocalDate;

/**
 * Base class for tests that are date based
 */
public abstract class DateTest extends Test {

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        try {
            LocalDate date = Conversions.toDate(text, context);
            if (evaluateForDate(runner, context, date)) {
                return Result.match(text, date);
            }
        }
        catch (EvaluationError ignored) {}

        return Result.NO_MATCH;
    }

    /**
     * Evaluates the test against the given decimal value. Subclasses must implement this.
     * @param runner the flow runner
     * @param context the evaluation context
     * @param date the date value
     * @return the test result
     */
    protected abstract boolean evaluateForDate(Runner runner, EvaluationContext context, LocalDate date);
}
