package io.rapidpro.flows.runner;

import io.rapidpro.flows.definition.Flow;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * Exception thrown when runner detects an infinite loop - i.e. we return to a previously visited node without having
 * waited for user input.
 */
public class FlowLoopException extends FlowRunException {

    protected Collection<Flow.Node> m_path;

    public FlowLoopException(Collection<Flow.Node> path) {
        super("Non-pausing loop detected after path:\n" + StringUtils.join(path, "\n"));

        m_path = path;
    }

    public Collection<Flow.Node> getPath() {
        return m_path;
    }
}
