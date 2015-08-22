package io.rapidpro.flows.runner;

import io.rapidpro.flows.FlowUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Input from the contact (for now this is always text)
 */
public class Input {

    protected String m_text;

    protected Instant m_time;

    public Input(String text) {
        m_text = text;
        m_time = Instant.now();
    }

    public Map<String, Object> buildContext(Org org) {
        Map<String, Object> context = new HashMap<>();
        context.put("*", m_text);
        context.put("value", m_text);
        context.put("time", FlowUtils.formatDate(m_time, org, true));

        // TODO include step.contact ?

        return context;
    }

    public String getText() {
        return m_text;
    }

    public Instant getTime() {
        return m_time;
    }
}
