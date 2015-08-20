package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;

import java.time.Instant;
import java.util.*;

/**
 * Implementation of the flow runner
 */
public class RunnerImpl implements Flows.Runner {

    /**
     * @see io.rapidpro.flows.Flows.Runner#start(Org, Contact, Flow)
     */
    @Override
    public RunState start(Org org, Contact contact, Flow flow) throws InfiniteLoopException {
        RunState run = new RunState(org, contact, flow);
        return resume(run, null);
    }

    /**
     * @see io.rapidpro.flows.Flows.Runner#resume(RunState, String)
     */
    @Override
    public RunState resume(RunState lastState, String input) throws InfiniteLoopException {
        RunState newState = new RunState(lastState.getOrg(), lastState.getContact(), lastState.getFlow());

        FlowStep lastStep = lastState.getSteps().size() > 0 ? lastState.getSteps().getLast() : null;

        // either we're resuming from a previous step that paused, or we're starting a new run
        Flow.Node currentNode = lastStep == null ? lastState.getFlow().getEntry() : lastStep.getNode();

        // tracks nodes visited so we can detect loops
        Set<Flow.Node> nodesVisited = new LinkedHashSet<>();

        do {
            FlowStep step = new FlowStep(currentNode);
            newState.getSteps().add(step);

            // should we pause at this node?
            if (currentNode instanceof RuleSet) {
                if (((RuleSet) currentNode).isPause() && nodesVisited.size() > 0) {
                    return newState;
                }
            }

            // check for an non-pausing loop
            if (nodesVisited.contains(currentNode)) {
                throw new InfiniteLoopException(nodesVisited);
            }
            nodesVisited.add(currentNode);

            Flow.Node nextNode = currentNode.visit(newState, step, input);
            if (nextNode != null) {
                step.setLeftOn(Instant.now());
            }

            currentNode = nextNode;
        }
        while (currentNode != null);

        return newState;
    }
}
