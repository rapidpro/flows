package io.rapidpro.excellent;

import java.util.List;

/**
 * Result of a template evaluation
 */
public class EvaluatedTemplate {

    private String m_content;

    private List<String> m_errors;

    EvaluatedTemplate(String m_content, List<String> m_errors) {
        this.m_content = m_content;
        this.m_errors = m_errors;
    }

    public String getContent() {
        return m_content;
    }

    public List<String> getErrors() {
        return m_errors;
    }
}
