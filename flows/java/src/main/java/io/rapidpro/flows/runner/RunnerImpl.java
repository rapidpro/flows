package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import org.threeten.bp.Instant;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of the flow runner
 */
public class RunnerImpl implements Flows.Runner {

    protected Location.Resolver m_locationResolver;

    public RunnerImpl(Location.Resolver locationResolver) {
        m_locationResolver = locationResolver;
    }

    /**
     * @see io.rapidpro.flows.Flows.Runner#start(Org, Contact, Flow)
     */
    @Override
    public RunState start(Org org, Contact contact, Flow flow) throws FlowRunException {
        RunState run = new RunState(org, contact, flow, m_locationResolver);
        return resume(run, null);
    }

    /**
     * @see io.rapidpro.flows.Flows.Runner#resume(RunState, Input)
     */
    @Override
    public RunState resume(RunState run, Input input) throws FlowRunException {
        if (run.getState().equals(RunState.State.COMPLETED)) {
            throw new IllegalStateException("Cannot resume a completed run state");
        }

        Step lastStep = run.getSteps().size() > 0 ? run.getSteps().get(run.getSteps().size() - 1) : null;

        // reset steps list so that it doesn't grow forever in a never-ending flow
        run.getSteps().clear();

        Flow.Node currentNode;
        if (lastStep != null) {
            currentNode = lastStep.getNode(); // we're resuming an existing run
        }
        else {
            currentNode = run.getFlow().getEntry();  // we're starting a new run
            if (currentNode == null) {
                throw new FlowRunException("Flow has no entry point");
            }
        }

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
            run.getSteps().add(step);

            // should we pause at this node?
            if (currentNode instanceof RuleSet) {
                if (((RuleSet) currentNode).isPause() && nodesVisited.size() > 0) {
                    run.setState(RunState.State.WAIT_MESSAGE);
                    return run;
                }
            }

            // check for an non-pausing loop
            if (nodesVisited.contains(currentNode)) {
                throw new FlowLoopException(nodesVisited);
            } else {
                nodesVisited.add(currentNode);
            }

            Flow.Node nextNode = currentNode.visit(run, step, input);

            if (nextNode != null) {
                // if we have a next node, then record leaving this one
                step.setLeftOn(Instant.now());
            } else {
                // if not then we've completed this flow
                run.setState(RunState.State.COMPLETED);
            }

            currentNode = nextNode;
        }
        while (currentNode != null);

        return run;
    }
}
