package io.rapidpro.flows.runner;

import io.rapidpro.flows.definition.Flow;

/**
 *
 */
public class Run {

    protected Flow m_flow;

    protected Contact m_contact;

    protected Flow.Node m_node;

    public Flow getFlow() {
        return m_flow;
    }

    public Contact getContact() {
        return m_contact;
    }

    public Flow.Node getNode() {
        return m_node;
    }
}
