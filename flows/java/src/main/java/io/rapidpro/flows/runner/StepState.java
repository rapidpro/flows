package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.definition.Flow;

import java.util.Map;

/**
 *
 */
public class StepState {

    @SerializedName("node")
    protected Flow.Node m_node;

    @SerializedName("variables")
    protected Map<String, Object> m_variables;

    public Flow.Node getNode() {
        return m_node;
    }

    public void setNode(Flow.Node node) {
        m_node = node;
    }

    public Map<String, Object> getVariables() {
        return m_variables;
    }
}
