package io.rapidpro.flows.runner;

import io.rapidpro.flows.definition.Action;
import io.rapidpro.flows.definition.Flow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FlowStep {

    protected Flow.Node m_node;

    protected Instant m_leftOn;

    List<Action.Result> m_actions;

    public FlowStep(Flow.Node node) {
        m_node = node;
        m_actions = new ArrayList<>();
    }

    public Flow.Node getNode() {
        return m_node;
    }

    public Instant getLeftOn() {
        return m_leftOn;
    }

    public void setLeftOn(Instant leftOn) {
        m_leftOn = leftOn;
    }

    public List<Action.Result> getActions() {
        return m_actions;
    }
}
