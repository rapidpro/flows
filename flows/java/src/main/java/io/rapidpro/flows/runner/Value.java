package io.rapidpro.flows.runner;

import io.rapidpro.flows.FlowUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Value {

    protected String m_value;

    protected String m_category;

    protected String m_text;

    protected Instant m_time;

    public Value(String value, String category, String text, Instant time) {
        m_value = value;
        m_category = category;
        m_text = text;
        m_time = time;
    }

    public Map<String, String> buildContext(Org org) {
        Map<String, String> context = new HashMap<>();
        context.put("*", m_value);
        context.put("value", m_value);
        context.put("category", m_category);
        context.put("text", m_text);
        context.put("time", FlowUtils.formatDate(m_time, org, true));
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
