package io.rapidpro.excellent;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link io.rapidpro.excellent.ExcellentEngine}
 */
public class ExcellentEngineTest {

    private ExcellentEngine m_engine = ExcellentEngine.getInstance();

    @Test
    public void evaluateExpression() {
        assertThat(m_engine.evaluateExpression("1 + 2", new HashMap<>()), is(new BigDecimal(3)));
        assertThat(m_engine.evaluateExpression("1.3 + 2.2", new HashMap<>()), is(new BigDecimal("3.5")));
        assertThat(m_engine.evaluateExpression("1.3 - 2.2", new HashMap<>()), is(new BigDecimal("-0.9")));
        assertThat(m_engine.evaluateExpression("4 * 2", new HashMap<>()), is(new BigDecimal(8)));
        assertThat(m_engine.evaluateExpression("4 / 2", new HashMap<>()), is(new BigDecimal(2)));

        Map<String, Object> context = new HashMap<>();
        context.put("foo", 5);
        context.put("bar", 3);
        assertThat(m_engine.evaluateExpression("FOO", context), is(5));
        assertThat(m_engine.evaluateExpression("foo + bar", context), is(new BigDecimal(8)));

        assertThat(m_engine.evaluateExpression("\"He said \"\"hi\"\" \"", new HashMap<>()), is("He said \"hi\""));

        assertThat(m_engine.evaluateExpression("PI()", new HashMap<>()), is(new BigDecimal(Math.PI)));
        assertThat(m_engine.evaluateExpression("pi()", new HashMap<>()), is(new BigDecimal(Math.PI)));
        //assertThat(m_engine.evaluateExpression("SUM(1, 2, 3)", new HashMap<>()), is(new BigDecimal(6)));
        //assertThat(m_engine.evaluateExpression("LEN(\"abc\")", new HashMap<>()), is(4));
    }
}
