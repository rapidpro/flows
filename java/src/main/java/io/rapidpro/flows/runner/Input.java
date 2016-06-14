package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Input from the contact or surveyor
 */
public class Input {

    protected Object m_value;

    protected Instant m_time;

    protected boolean m_consumed;

    protected Input(Object value) {
        m_value = value;
        m_time = Instant.now();
        m_consumed = false;
    }

    public static final class MediaResource {
        private String m_type;
        private String m_url;

        public MediaResource(String mediaType, String url) {
            m_type = mediaType;
            m_url = url;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(m_type).append(":").append(m_url);
            return sb.toString();
        }

        public String getUrl() {
            return m_url;
        }
    }

    public static Input of(String mediaType, String url) {
        return new Input(new MediaResource(mediaType, url));
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
     * @param container the containing evaluation context
     * @param contactContext the contact context
     * @return the context
     */
    public Map<String, Object> buildContext(EvaluationContext container, Map<String, String> contactContext) {
        Map<String, Object> context = new HashMap<>();
        String asText = getValueAsText(container);

        context.put("*", asText);
        context.put("value", asText);
        context.put("time", Conversions.toString(m_time.atZone(container.getTimezone()), container));
        context.put("contact", contactContext);

        return context;
    }

    /**
     * Gets the input value as text which can be matched by rules
     * @param context the evaluation context
     * @return the text value
     */
    public String getValueAsText(EvaluationContext context) {
        if (m_value instanceof MediaResource) {
            return ((MediaResource) m_value).getUrl();
        }
        return Conversions.toString(m_value, context);
    }

    public String getMedia() {
        if (m_value instanceof MediaResource) {
            return m_value.toString();
        }
        return null;
    }

    public Instant getTime() {
        return m_time;
    }

    public boolean isConsumed() {
        return m_consumed;
    }

    public void consume() {
        m_consumed = true;
    }

}
