package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;

import java.math.BigDecimal;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Input from the contact or surveyor
 */
public class Input {

    protected Object m_value;

    protected Instant m_time;

    protected Input(Object value) {
        m_value = value;
        m_time = Instant.now();
    }

    public static Input of(String value) {
        return new Input(value);
    }

    public static Input of(BigDecimal value) {
        return new Input(value);
    }

    public static Input of(LocalDate value) {
        return new Input(value);
    }

    public static Input of(ZonedDateTime value) {
        return new Input(value);
    }

    /**
     * Builds the evaluation context for this input
     * @param container the evaluation context
     * @return the context
     */
    public Map<String, String> buildContext(EvaluationContext container) {
        Map<String, String> context = new HashMap<>();
        String asText = getValueAsText(container);

        context.put("*", asText);
        context.put("value", asText);
        context.put("time", Conversions.toString(m_time.atZone(container.getTimezone()), container));

        // TODO include step.contact ?

        return context;
    }

    /**
     * Gets the input value as text which can be matched by rules
     * @param context the evaluation context
     * @return the text value
     */
    public String getValueAsText(EvaluationContext context) {
        return Conversions.toString(m_value, context);
    }

    public Instant getTime() {
        return m_time;
    }
}
