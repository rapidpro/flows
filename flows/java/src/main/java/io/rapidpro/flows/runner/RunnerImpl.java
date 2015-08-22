package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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
    public RunState resume(RunState lastState, String text) throws InfiniteLoopException {
        if (lastState.getState().equals(RunState.State.COMPLETED)) {
            throw new IllegalStateException("Cannot resume a completed run state");
        }

        RunState newState = new RunState(lastState.getOrg(), lastState.getContact(), lastState.getFlow());
        Input input = text != null ? new Input(text) : null;

        Step lastStep = lastState.getSteps().size() > 0 ? lastState.getSteps().getLast() : null;

        // either we're resuming from a previous step that paused, or we're starting a new run
        Flow.Node currentNode = lastStep == null ? lastState.getFlow().getEntry() : lastStep.getNode();

        // tracks nodes visited so we can detect loops
        Set<Flow.Node> nodesVisited = new LinkedHashSet<>();

        do {
            // if we're resuming a previously paused step, then use it's arrived on value
            Instant arrivedOn;
            if (lastStep != null && nodesVisited.size() == 0) {
                arrivedOn = lastStep.getArrivedOn();
            } else {
                arrivedOn = Instant.now();
            }

            // create new step for this node
            Step step = new Step(currentNode, arrivedOn);
            newState.getSteps().add(step);

            // should we pause at this node?
            if (currentNode instanceof RuleSet) {
                if (((RuleSet) currentNode).isPause() && nodesVisited.size() > 0) {
                    newState.setState(RunState.State.WAIT_MESSAGE);
                    return newState;
                }
            }

            // check for an non-pausing loop
            if (nodesVisited.contains(currentNode)) {
                throw new InfiniteLoopException(nodesVisited);
            } else {
                nodesVisited.add(currentNode);
            }

            Flow.Node nextNode = currentNode.visit(newState, step, input);

            if (nextNode != null) {
                // if we have a next node, then record leaving this one
                step.setLeftOn(Instant.now());
            } else {
                // if not then we've completed this flow
                newState.setState(RunState.State.COMPLETED);
            }

            currentNode = nextNode;
        }
        while (currentNode != null);

        return newState;
    }
}
