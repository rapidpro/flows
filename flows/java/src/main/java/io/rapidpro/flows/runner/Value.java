package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Value {

    @SerializedName("value")
    protected String m_value;

    @SerializedName("category")
    protected String m_category;

    @SerializedName("text")
    protected String m_text;

    protected Instant m_time;

    public Value(String value, String category, String text, Instant time) {
        m_value = value;
        m_category = category;
        m_text = text;
        m_time = time;
    }

    public Map<String, String> buildContext(EvaluationContext container) {
        Map<String, String> context = new HashMap<>();
        context.put("*", m_value);
        context.put("value", m_value);
        context.put("category", m_category);
        context.put("text", m_text);
        context.put("time", Conversions.toString(m_time.atZone(container.getTimezone()), container));
        return context;
    }

    public String getValue() {
        return m_value;
    }

    public String getCategory() {
        return m_category;
    }

    public String getText() {
        return m_text;
    }

    public Instant getTime() {
        return m_time;
    }
}
