package io.rapidpro.expressions;

import java.util.List;

/**
 * Result of a template evaluation
 */
public class EvaluatedTemplate {

    private String m_output;

    private List<String> m_errors;

    public EvaluatedTemplate(String m_output, List<String> m_errors) {
        this.m_output = m_output;
        this.m_errors = m_errors;
    }

    public String getOutput() {
        return m_output;
    }

    public List<String> getErrors() {
        return m_errors;
    }
}
