package io.rapidpro.expressions.evaluator;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.EvaluationError;
import io.rapidpro.expressions.Expressions;
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

    private Expressions.TemplateEvaluator m_evaluator = new TemplateEvaluatorImpl();

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
        assertThat(m_evaluator.evaluateExpression("true", new EvaluationContext()), is((Object) true));
        assertThat(m_evaluator.evaluateExpression("FALSE", new EvaluationContext()), is((Object) false));

        assertThat(m_evaluator.evaluateExpression("10", new EvaluationContext()), is((Object) new BigDecimal(10)));
        assertThat(m_evaluator.evaluateExpression("1234.5678", new EvaluationContext()), is((Object) new BigDecimal("1234.5678")));

        assertThat(m_evaluator.evaluateExpression("\"\"", new EvaluationContext()), is((Object) ""));
        assertThat(m_evaluator.evaluateExpression("\"سلام\"", new EvaluationContext()), is((Object) "سلام"));
        assertThat(m_evaluator.evaluateExpression("\"He said \"\"hi\"\" \"", new EvaluationContext()), is((Object) "He said \"hi\" "));

        assertThat(m_evaluator.evaluateExpression("-10", new EvaluationContext()), is((Object) new BigDecimal(-10)));
        assertThat(m_evaluator.evaluateExpression("1 + 2", new EvaluationContext()), is((Object) new BigDecimal(3)));
        assertThat(m_evaluator.evaluateExpression("1.3 + 2.2", new EvaluationContext()), is((Object) new BigDecimal("3.5")));
        assertThat(m_evaluator.evaluateExpression("1.3 - 2.2", new EvaluationContext()), is((Object) new BigDecimal("-0.9")));
        assertThat(m_evaluator.evaluateExpression("4 * 2", new EvaluationContext()), is((Object) new BigDecimal(8)));
        assertThat(m_evaluator.evaluateExpression("4 / 2", new EvaluationContext()), is((Object) new BigDecimal("2.0000000000")));
        assertThat(m_evaluator.evaluateExpression("4 ^ 2", new EvaluationContext()), is((Object) new BigDecimal(16)));
        assertThat(m_evaluator.evaluateExpression("4 ^ 0.5", new EvaluationContext()), is((Object) new BigDecimal(2)));
        assertThat(m_evaluator.evaluateExpression("4 ^ -1", new EvaluationContext()), is((Object) new BigDecimal("0.25")));

        assertThat(m_evaluator.evaluateExpression("\"foo\" & \"bar\"", new EvaluationContext()), is((Object) "foobar"));
        assertThat(m_evaluator.evaluateExpression("2 & 3 & 4", new EvaluationContext()), is((Object) "234"));

        // check precedence
        assertThat(m_evaluator.evaluateExpression("2 + 3 / 4 - 5 * 6", new EvaluationContext()), is((Object) new BigDecimal("-27.2500000000")));
        assertThat(m_evaluator.evaluateExpression("2 & 3 + 4 & 5", new EvaluationContext()), is((Object) "275"));

        // check associativity
        assertThat(m_evaluator.evaluateExpression("2 - -2 + 7", new EvaluationContext()), is((Object) new BigDecimal(11)));
        assertThat(m_evaluator.evaluateExpression("2 ^ 3 ^ 4", new EvaluationContext()), is((Object) new BigDecimal(4096)));

        EvaluationContext context = new EvaluationContext();
        context.putVariable("foo", 5);
        context.putVariable("bar", 3);
        assertThat(m_evaluator.evaluateExpression("FOO", context), is((Object) 5));
        assertThat(m_evaluator.evaluateExpression("foo + bar", context), is((Object) new BigDecimal(8)));

        assertThat(m_evaluator.evaluateExpression("len(\"abc\")", new EvaluationContext()), is((Object) 3));
        assertThat(m_evaluator.evaluateExpression("SUM(1, 2, 3)", new EvaluationContext()), is((Object) new BigDecimal(6)));

        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678)", new EvaluationContext()), is((Object) "1,234.57"));
        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678, 1)", new EvaluationContext()), is((Object) "1,234.6"));
        assertThat(m_evaluator.evaluateExpression("FIXED(1234.5678, 1, True)", new EvaluationContext()), is((Object) "1234.6"));
    }

    @Test
    public void evaluateExpression_withErrors() {
        EvaluationContext context = new EvaluationContext();
        context.putVariable("foo", 5);

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
