package io.rapidpro.flows.runner;

import io.rapidpro.flows.definition.Flow;

import java.util.Collection;
import java.util.List;

/**
 * Exception thrown when runner detects an infinite loop - i.e. we return to a previously visited node without having
 * waited for user input.
 */
public class InfiniteLoopException extends Exception {

    protected Collection<Flow.Node> m_path;

    public InfiniteLoopException(Collection<Flow.Node> path) {
        m_path = path;
    }

    public Collection<Flow.Node> getPath() {
        return m_path;
    }
}
