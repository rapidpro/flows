package io.rapidpro.excellent.evaluator;

import io.rapidpro.excellent.EvaluatedTemplate;
import io.rapidpro.excellent.EvaluationContext;
import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.Excellent;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TemplateEvaluatorImpl}
 */
public class TemplateEvaluatorImplTest {

    private Excellent.TemplateEvaluator m_evaluator = new TemplateEvaluatorImpl();

    @Test
    public void evaluateTemplate() {
        EvaluatedTemplate evaluated = m_evaluator.evaluateTemplate("Answer is @(2 + 3)", new EvaluationContext());
        assertThat(evaluated.getOutput(), is("Answer is 5"));
        assertThat(evaluated.getErrors(), empty());
    }

    @Test
    public void evaluateTemplate_unbalancedExpression() {
        EvaluatedTemplate evaluated = m_evaluator.evaluateTemplate("Answer is @(2 + 3", new EvaluationContext());
        assertThat(evaluated.getOutput(), is("Answer is @(2 + 3"));
        assertThat(evaluated.getErrors(), empty());
    }

    @Test
    public void evaluateExpression() {
        assertThat(m_evaluator.evaluateExpression("true", new EvaluationContext()), is(true));
        assertThat(m_evaluator.evaluateExpression("FALSE", new EvaluationContext()), is(false));

        assertThat(m_evaluator.evaluateExpression("10", new EvaluationContext()), is(new BigDecimal(10)));
        assertThat(m_evaluator.evaluateExpression("1234.5678", new EvaluationContext()), is(new BigDecimal("1234.5678")));

        assertThat(m_evaluator.evaluateExpression("\"\"", new EvaluationContext()), is(""));
        assertThat(m_evaluator.evaluateExpression("\"سلام\"", new EvaluationContext()), is("سلام"));
        assertThat(m_evaluator.evaluateExpression("\"He said \"\"hi\"\" \"", new EvaluationContext()), is("He said \"hi\" "));

        assertThat(m_evaluator.evaluateExpression("-10", new EvaluationContext()), is(new BigDecimal(-10)));
        assertThat(m_evaluator.evaluateExpression("1 + 2", new EvaluationContext()), is(new BigDecimal(3)));
        assertThat(m_evaluator.evaluateExpression("1.3 + 2.2", new EvaluationContext()), is(new BigDecimal("3.5")));
        assertThat(m_evaluator.evaluateExpression("1.3 - 2.2", new EvaluationContext()), is(new BigDecimal("-0.9")));
        assertThat(m_evaluator.evaluateExpression("4 * 2", new EvaluationContext()), is(new BigDecimal(8)));
        assertThat(m_evaluator.evaluateExpression("4 / 2", new EvaluationContext()), is(new BigDecimal(2)));
        assertThat(m_evaluator.evaluateExpression("4 ^ 2", new EvaluationContext()), is(new BigDecimal(16)));
        assertThat(m_evaluator.evaluateExpression("4 ^ 0.5", new EvaluationContext()), is(new BigDecimal(2)));
        assertThat(m_evaluator.evaluateExpression("4 ^ -1", new EvaluationContext()), is(new BigDecimal("0.25")));

        assertThat(m_evaluator.evaluateExpression("\"foo\" & \"bar\"", new EvaluationContext()), is("foobar"));
        assertThat(m_evaluator.evaluateExpression("2 & 3 & 4", new EvaluationContext()), is("234"));

        // check precedence
        assertThat(m_evaluator.evaluateExpression("2 + 3 / 4 - 5 * 6", new EvaluationContext()), is(new BigDecimal("-27.25")));
        assertThat(m_evaluator.evaluateExpression("2 & 3 + 4 & 5", new EvaluationContext()), is("275"));

        // check associativity
        assertThat(m_evaluator.evaluateExpression("2 - -2 + 7", new EvaluationContext()), is(new BigDecimal(11)));
        assertThat(m_evaluator.evaluateExpression("2 ^ 3 ^ 4", new EvaluationContext()), is(new BigDecimal(4096)));

        EvaluationContext context = new EvaluationContext();
        context.put("foo", 5);
        context.put("bar", 3);
        assertThat(m_evaluator.evaluateExpression("FOO", context), is(5));
        assertThat(m_evaluator.evaluateExpression("foo + bar", context), is(new BigDecimal(8)));

        assertThat(m_evaluator.evaluateExpression("len(\"abc\")", new EvaluationContext()), is(3));
        assertThat(m_evaluator.evaluateExpression("SUM(1, 2, 3)", new EvaluationContext()), is(new BigDecimal(6)));

        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678)", new EvaluationContext()), is("1,234.57"));
        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678, 1)", new EvaluationContext()), is("1,234.6"));
        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678, 1, True)", new EvaluationContext()), is("1234.6"));
    }

    @Test
    public void evaluateExpression_withErrors() {
        EvaluationContext context = new EvaluationContext();
        context.put("foo", 5);

        // parser errors
        assertErrorMessage("0 /", context, "Expression is invalid");
        assertErrorMessage("\"", context, "Expression is invalid");
        assertErrorMessage("1.1.0", context, "Expression is invalid");

        // evaluation errors
        assertErrorMessage("X", context, "No item called X in context");
        assertErrorMessage("2 / 0", context, "Division by zero");
        assertErrorMessage("0 / 0", context, "Division by zero");
    }

    protected void assertErrorMessage(String expression, EvaluationContext context, String expectedMessage) {
        try {
            m_evaluator.evaluateExpression(expression, context);
            Assert.fail();
        }
        catch (EvaluationError ex) {
            assertThat(ex.getMessage(), is(expectedMessage));
        }
    }
}
