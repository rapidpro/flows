package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.EvaluatedTemplate;
import io.rapidpro.excellent.EvaluationContext;
import io.rapidpro.excellent.Excellent;
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
        assertThat(evaluated.getContent(), is("Answer is 5"));
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

        assertThat(m_evaluator.evaluateExpression("1 + 2", new EvaluationContext()), is(new BigDecimal(3)));
        assertThat(m_evaluator.evaluateExpression("1.3 + 2.2", new EvaluationContext()), is(new BigDecimal("3.5")));
        assertThat(m_evaluator.evaluateExpression("1.3 - 2.2", new EvaluationContext()), is(new BigDecimal("-0.9")));
        assertThat(m_evaluator.evaluateExpression("4 * 2", new EvaluationContext()), is(new BigDecimal(8)));
        assertThat(m_evaluator.evaluateExpression("4 / 2", new EvaluationContext()), is(new BigDecimal(2)));

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
}
