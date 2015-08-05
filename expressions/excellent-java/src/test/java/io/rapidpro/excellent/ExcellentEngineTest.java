package io.rapidpro.excellent;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link io.rapidpro.excellent.ExcellentEngine}
 */
public class ExcellentEngineTest {

    private ExcellentEngine m_engine = ExcellentEngine.getInstance();

    @Test
    public void evaluateExpression() {
        assertThat(m_engine.evaluateExpression("true", new EvaluationContext()), is(true));
        assertThat(m_engine.evaluateExpression("FALSE", new EvaluationContext()), is(false));

        assertThat(m_engine.evaluateExpression("10", new EvaluationContext()), is(new BigDecimal(10)));
        assertThat(m_engine.evaluateExpression("1234.5678", new EvaluationContext()), is(new BigDecimal("1234.5678")));

        assertThat(m_engine.evaluateExpression("\"\"", new EvaluationContext()), is(""));
        assertThat(m_engine.evaluateExpression("\"سلام\"", new EvaluationContext()), is("سلام"));
        assertThat(m_engine.evaluateExpression("\"He said \"\"hi\"\" \"", new EvaluationContext()), is("He said \"hi\" "));

        assertThat(m_engine.evaluateExpression("1 + 2", new EvaluationContext()), is(new BigDecimal(3)));
        assertThat(m_engine.evaluateExpression("1.3 + 2.2", new EvaluationContext()), is(new BigDecimal("3.5")));
        assertThat(m_engine.evaluateExpression("1.3 - 2.2", new EvaluationContext()), is(new BigDecimal("-0.9")));
        assertThat(m_engine.evaluateExpression("4 * 2", new EvaluationContext()), is(new BigDecimal(8)));
        assertThat(m_engine.evaluateExpression("4 / 2", new EvaluationContext()), is(new BigDecimal(2)));

        EvaluationContext context = new EvaluationContext();
        context.put("foo", 5);
        context.put("bar", 3);
        assertThat(m_engine.evaluateExpression("FOO", context), is(5));
        assertThat(m_engine.evaluateExpression("foo + bar", context), is(new BigDecimal(8)));

        assertThat(m_engine.evaluateExpression("len(\"abc\")", new EvaluationContext()), is(3));
        assertThat(m_engine.evaluateExpression("SUM(1, 2, 3)", new EvaluationContext()), is(new BigDecimal(6)));

        assertThat(m_engine.evaluateExpression("FIXED(1234.5678)", new EvaluationContext()), is("1,234.57"));
        assertThat(m_engine.evaluateExpression("FIXED(1234.5678, 1)", new EvaluationContext()), is("1,234.6"));
        assertThat(m_engine.evaluateExpression("FIXED(1234.5678, 1, True)", new EvaluationContext()), is("1234.6"));
    }
}
