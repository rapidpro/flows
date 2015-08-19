package io.rapidpro.flows.runner;

import io.rapidpro.flows.Flows;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the flow runner
 */
public class RunnerImpl implements Flows.Runner {

    public RunState newRun(Org org, Contact contact, Flow flow) {
        RunState run = new RunState(org, contact, flow);
        return resume(run, null);
    }

    public RunState resume(RunState lastState, String input) {
        RunState newState = new RunState(lastState.getOrg(), lastState.getContact(), lastState.getFlow());

        Flow.Node currentNode = lastState.getStepState().getNode();
        if (currentNode == null) {
            // starting a new run
            currentNode = lastState.getFlow().getEntry();
        }

        List<Flow.Node> path = new ArrayList<>();

        do {
            newState.getStepState().setNode(currentNode);

            if (currentNode instanceof RuleSet) {
                if (((RuleSet) currentNode).isPause() && path.size() > 0) {
                    return newState;
                }
            }

            if (path.contains(currentNode)) {
                throw new RuntimeException("Flow loop detected");
            }

            path.add(currentNode);

            currentNode = currentNode.visit(newState, input);
        }
        while (currentNode != null);

        return newState;
    }
}
