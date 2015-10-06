package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Evaluator;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Instant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the flow runner
 */
public class Runner {

    protected Evaluator m_templateEvaluator;

    protected Location.Resolver m_locationResolver;

    public Runner(Evaluator templateEvaluator, Location.Resolver locationResolver) {
        m_templateEvaluator = templateEvaluator;
        m_locationResolver = locationResolver;
    }

    /**
     * Starts a new run
     * @param org the org
     * @param fields the contact fields
     * @param contact the contact
     * @param flow the flow
     * @return the run state
     */
    public RunState start(Org org, List<Field> fields, Contact contact, Flow flow) throws FlowRunException {
        RunState run = new RunState(org, fields, contact, flow);
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
     * Performs partial variable substitution on the the given text
     * @param text the text, e.g. "Hi @contact.name"
     * @param context the evaluation context
     * @return the evaluated template, e.g. "Hi Joe"
     */
    public EvaluatedTemplate substituteVariablesIfAvailable(String text, EvaluationContext context) {
        return m_templateEvaluator.evaluateTemplate(text, context, false, Evaluator.EvaluationStrategy.RESOLVE_AVAILABLE);
    }

    public Evaluator getTemplateEvaluator() {
        return m_templateEvaluator;
    }

    /**
     * Parses a location from the given text
     * @param text the text containing a location name
     * @param country the 2-digit country code
     * @param level the level
     * @param parent the parent location (may be null)
     * @return the location or null if no such location exists
     */
    public Location parseLocation(String text, String country, Location.Level level, Location parent) {
        if (m_locationResolver != null) {
            return m_locationResolver.resolve(text, country, level, parent);
        }
        return null;
    }

    /**
     * Updates a field on the contact for the given run
     * @param run the current run state
     * @param key the field key
     * @param value the field value
     */
    public void updateContactField(RunState run, String key, String value) {
        Field field = run.getOrCreateField(key);
        String actualValue = null;

        switch (field.getValueType()) {
            case TEXT:
            case DECIMAL:
            case DATETIME:
                actualValue = value;
                break;
            case STATE: {
                Location state = m_locationResolver.resolve(value, run.getOrg().getCountry(), Location.Level.STATE, null);
                if (state != null) {
                    actualValue = state.getName();
                }
                break;
            }
            case DISTRICT: {
                Field stateField = getStateField(run);
                if (stateField != null) {
                    String stateName = run.getContact().getFields().get(stateField.getKey());
                    if (StringUtils.isNotEmpty(stateName)) {
                        Location state = m_locationResolver.resolve(stateName, run.getOrg().getCountry(), Location.Level.STATE, null);
                        if (state != null) {
                            Location district = m_locationResolver.resolve(value, run.getOrg().getCountry(), Location.Level.DISTRICT, state);
                            if (district != null) {
                                actualValue = district.getName();
                            }
                        }
                    }
                }
                break;
            }
        }

        run.getContact().getFields().put(key, actualValue);
    }

    /**
     * Updates the extra key value store for the given run state
     * @param run the run state
     * @param values the key values
     */
    public void updateExtra(RunState run, Map<String, ?> values) {
        run.getExtra().putAll(values);
    }

    /**
     * TODO this mimics what we currently do in RapidPro but needs changed
     */
    public Field getStateField(RunState run) {
        for (Field field : run.m_fields) {
            if (field.getValueType().equals(Field.ValueType.STATE)) {
                return field;
            }
        }
        return null;
    }
}
