package io.rapidpro.flows.definition.tests.numeric;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for tests that are numerical
 */
public abstract class NumericTest extends Test {

    /**
     * A very flexible decimal parser
     * @param text the text to be parsed
     * @return the decimal value
     */
    protected static BigDecimal extractDecimal(String text) {
        // common substitutions
        String originalText = text;
        text = text.replace('l', '1').replace('o', '0').replace('O', '0');

        try {
            return new BigDecimal(text);
        }
        catch (NumberFormatException ex) {
            // we only try this hard if we haven 't already substituted characters
            if (originalText.equals(text)) {
                // does this start with a number? just use that part if so
                Matcher matcher = Pattern.compile("^(\\d+).*$").matcher(text);
                if (matcher.matches()) {
                    return new BigDecimal(matcher.group(1));
                }
            }
            throw ex;
        }
    }

    /**
     * @see Test#evaluate(Runner, RunState, EvaluationContext, String)
     */
    @Override
    public Result evaluate(Runner runner, RunState run, EvaluationContext context, String text) {
        text = text.replace(",", ""); // so that 1,234 is parsed as 1234

        // test every word in the message against our test
        for (String word : Pattern.compile("\\s+").split(text)) {
            try {
                BigDecimal decimal = extractDecimal(word);
                if (evaluateForDecimal(runner, context, decimal)) {
                    return Test.Result.match(decimal);
                }
            }
            catch (NumberFormatException ignored) {}
        }

        return Result.NO_MATCH;
    }

    /**
     * Evaluates the test against the given decimal value. Subclasses must implement this.
     * @param runner the flow runner
     * @param context the evaluation context
     * @param decimal the decimal value
     * @return the test result
     */
    protected abstract boolean evaluateForDecimal(Runner runner, EvaluationContext context, BigDecimal decimal);
}
