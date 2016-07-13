package io.rapidpro.flows.runner;

import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Evaluator;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Instant;

import java.util.*;

/**
 * Implementation of the flow runner
 */
public class Runner {

    protected Evaluator m_templateEvaluator;

    protected Location.Resolver m_locationResolver;

    protected Instant m_now;

    protected Map<String,Flow> m_flows;

    public Runner(Evaluator templateEvaluator, Location.Resolver locationResolver, Instant now, List<Flow> flows) {
        m_templateEvaluator = templateEvaluator;
        m_locationResolver = locationResolver;
        m_now = now;

        // create a map of flow uuid to flow
        m_flows = new HashMap<>();
        for (Flow flow : flows) {
            m_flows.put(flow.getUuid(), flow);
        }
    }

    /**
     * Starts a new run
     * @param org the org
     * @param fields the contact fields
     * @param contact the contact
     * @param flowUuid the id of the flow to start
     * @return the run state
     */
    public RunState start(Org org, List<Field> fields, Contact contact, String flowUuid) throws FlowRunException {
        RunState run = new RunState(org, fields, contact, m_flows);
        run.setActiveFlow(flowUuid);
        return resume(run, null);
    }

    /**
     * Starts a new run
     * @param org the org
     * @param fields the contact fields
     * @param contact the contact
     * @param flow the flow to start
     * @return the run state
     */
    public RunState start(Org org, List<Field> fields, Contact contact, Flow flow) throws FlowRunException {
        m_flows.put(flow.getUuid(), flow);
        return start(org, fields, contact, flow.getUuid());
    }

    /**
     * Resumes an existing run with new input
     * @param run the previous run state
     * @param input the new input
     * @return the updated run state
     */
    public RunState resume(RunState run, Input input) throws FlowRunException {
        if (run.getState().equals(RunState.State.COMPLETED)) {
            throw new FlowRunException("Cannot resume a completed run");
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

        Step resumeStep = null;
        while (currentNode != null) {
            // if we're resuming a previously paused step, then use its arrived on value
            Instant arrivedOn;
            if (lastStep != null && nodesVisited.size() == 0) {
                arrivedOn = lastStep.getArrivedOn();
            } else {
                arrivedOn = Instant.now();
            }

            // create our step for the current node
            Step step = new Step(run.getFlow(), currentNode, arrivedOn);

            // if we are resuming an old step, use that instead
            if (resumeStep != null) {
                step = resumeStep;
                currentNode = step.getNode();
            }
            // otherwise add our step to our step list
            else {
                run.getSteps().add(step);
            }

            // see if we need to dive into a subflow
            if (currentNode instanceof RuleSet) {
                RuleSet ruleset = (RuleSet) currentNode;
                if (resumeStep == null && ruleset.isSubflow() && (lastStep == null || !ruleset.getUuid().equals(lastStep.getNode().getUuid()))) {
                    run.enterSubflow(step, ruleset.getSubflowUuid());
                    currentNode = run.getFlow().getEntry();

                    // create our new step accordingly
                    step = new Step(run.getFlow(), currentNode, arrivedOn);
                    run.getSteps().add(step);
                }
            }

            // no longer resuming
            resumeStep = null;

            // should we pause at this node?
            if (currentNode instanceof RuleSet) {
                RuleSet ruleset = (RuleSet) currentNode;

                if (ruleset.isPause() && (input == null || input.isConsumed())) {
                    // set our waiting state appropriately
                    if (ruleset.getRuleSetType() == RuleSet.Type.WAIT_GPS) {
                        run.setState(RunState.State.WAIT_GPS);
                    } else if (ruleset.getRuleSetType() == RuleSet.Type.WAIT_PHOTO) {
                        run.setState(RunState.State.WAIT_PHOTO);
                    } else if (ruleset.getRuleSetType() == RuleSet.Type.WAIT_VIDEO) {
                        run.setState(RunState.State.WAIT_VIDEO);
                    } else if (ruleset.getRuleSetType() == RuleSet.Type.WAIT_AUDIO) {
                        run.setState(RunState.State.WAIT_AUDIO);
                    } else {
                        run.setState(RunState.State.WAIT_MESSAGE);
                    }
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
            }
            // if not then we've completed this flow
            else {

                step.setTerminal(true);

                // if its at the lowest level, then we are done
                if (run.m_level == 0) {
                    run.setState(RunState.State.COMPLETED);
                }
                // otherwise, we are going up a level
                else {
                    resumeStep = run.exitSubflow();
                    nextNode = resumeStep.getNode();
                }
            }

            currentNode = nextNode;
        }

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
     * @return the field which may have been created
     */
    public Field updateContactField(RunState run, String key, String value) {
        return updateContactField(run, key, value, null);
    }

    /**
     * Updates a field on the contact for the given run
     * @param run the current run state
     * @param key the field key
     * @param value the field value
     * @param label the field label (may be null)
     * @return the field which may have been created
     */
    public Field updateContactField(RunState run, String key, String value, String label) {
        Field field = run.getOrCreateField(key, label);
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
                Field stateField = getLocationField(run, Field.ValueType.STATE);
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
            case WARD: {
                Field stateField = getLocationField(run, Field.ValueType.STATE);
                Field districtField = getLocationField(run, Field.ValueType.DISTRICT);
                if (stateField != null && districtField != null) {
                    String stateName = run.getContact().getFields().get(stateField.getKey());
                    String districtName = run.getContact().getFields().get(districtField.getKey());
                    if (StringUtils.isNotEmpty(stateName) && StringUtils.isNotEmpty(districtName)) {
                        Location state = m_locationResolver.resolve(stateName, run.getOrg().getCountry(), Location.Level.STATE, null);
                        if (state != null) {
                            Location district = m_locationResolver.resolve(districtName, run.getOrg().getCountry(), Location.Level.DISTRICT, state);
                            if (district != null) {
                                Location ward = m_locationResolver.resolve(value, run.getOrg().getCountry(), Location.Level.WARD, district);
                                if (ward != null) {
                                    actualValue = ward.getName();
                                }
                            }
                        }
                    }
                }
                break;
            }
        }

        run.getContact().getFields().put(field.getKey(), actualValue);
        return field;
    }

    /**
     * Updates the extra key value store for the given run state
     * @param run the run state
     * @param values the key values
     */
    public void updateExtra(RunState run, Map<String, String> values) {
        run.getExtra().putAll(values);
    }

    /**
     * TODO this mimics what we currently do in RapidPro but needs changed
     */
    public Field getLocationField(RunState run, Field.ValueType type) {
        for (Field field : run.m_fields) {
            if (field.getValueType().equals(type)) {
                return field;
            }
        }
        return null;
    }

    public Instant getNow() {
        return m_now;
    }
}
