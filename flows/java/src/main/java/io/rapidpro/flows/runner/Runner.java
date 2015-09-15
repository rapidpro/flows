package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.TemplateEvaluator;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import org.threeten.bp.Instant;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of the flow runner
 */
public class Runner {

    protected TemplateEvaluator m_templateEvaluator;

    protected Field.Provider m_fieldProvider;

    protected Location.Resolver m_locationResolver;

    public Runner(TemplateEvaluator templateEvaluator, Field.Provider fieldProvider, Location.Resolver locationResolver) {
        m_templateEvaluator = templateEvaluator;
        m_fieldProvider = fieldProvider;
        m_locationResolver = locationResolver;
    }

    /**
     * Starts a new run
     * @param org the org
     * @param contact the contact
     * @param flow the flow
     * @return the run state
     */
    public RunState start(Org org, Contact contact, Flow flow) throws FlowRunException {
        RunState run = RunState.start(org, contact, flow);
        return resume(run, null);
    }

    /**
     * Resumes an existing run with new input
     * @param run the previous run state
     * @param input the new input
     * @return the updated run state
     */
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

            Flow.Node nextNode = currentNode.visit(this, run, step, input);

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

    /**
     * Performs variable substitution on the the given text
     * @param text the text, e.g. "Hi @contact.name"
     * @param context the evaluation context
     * @return the evaluated template, e.g. "Hi Joe"
     */
    public EvaluatedTemplate substituteVariables(String text, EvaluationContext context) {
        return m_templateEvaluator.evaluateTemplate(text, context);
    }

    /**
     * Performs variable substitution on the the given text
     * @param text the text, e.g. "Hi @contact.name"
     * @param context the evaluation context
     * @return the evaluated template, e.g. "Hi Joe"
     */
    public EvaluatedTemplate substituteVariablesIfAvailable(String text, EvaluationContext context) {
        return m_templateEvaluator.evaluateTemplate(text, context, false, TemplateEvaluator.EvaluationStrategy.RESOLVE_AVAILABLE);
    }

    public TemplateEvaluator getTemplateEvaluator() {
        return m_templateEvaluator;
    }

    /**
     * Gets the location resolver used by this runner
     * @return the resolver
     */
    public Location.Resolver getLocationResolver() {
        return m_locationResolver;
    }

    /**
     * Updates a field on the contact for the given run
     * @param run the current run state
     * @param key the field key
     * @param value the field value
     */
    public void updateContactField(RunState run, String key, String value) {
        Field field = m_fieldProvider.provide(key);
        if (field == null) {
            field = new Field(key, null, Field.ValueType.TEXT);
        }

        String actualValue = null;

        switch (field.getValueType()) {
            case TEXT:
            case DECIMAL:
            case DATETIME:
                actualValue = value;
            case STATE: {
                Location state = m_locationResolver.resolve(value, run.getOrg().getCountry(), Location.Level.STATE, null);
                if (state != null) {
                    actualValue = state.getName();
                }
            }
            case DISTRICT: {
                String stateFieldKey = run.getOrg().getStateField();
                if (stateFieldKey != null) {
                    String stateName = run.getContact().getFields().get(stateFieldKey);
                    Location district = m_locationResolver.resolve(value, run.getOrg().getCountry(), Location.Level.DISTRICT, stateName);
                    if (district != null) {
                        actualValue = district.getName();
                    }
                }
            }
        }

        run.getContact().setField(key, actualValue);
    }
}
