package io.rapidpro.flows.runner;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Input from the contact (for now this is always text)
 */
public class Input {

    protected String m_text;

    protected Instant m_createdOn;

    public Input(String text) {
        m_text = text;
        m_createdOn = Instant.now();
    }

    public Map<String, Object> buildContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("*", m_text);
        context.put("value", m_text);
        context.put("time", m_createdOn);

        // TODO include step.contact

        return context;
    }

    public String getText() {
        return m_text;
    }

    public Instant getCreatedOn() {
        return m_createdOn;
    }
}
