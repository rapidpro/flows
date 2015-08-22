package io.rapidpro.flows.runner;

import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.Rule;
import io.rapidpro.flows.definition.actions.Action;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Step {

    protected Flow.Node m_node;

    protected Instant m_arrivedOn;

    protected Instant m_leftOn;

    protected Rule.Result m_ruleResult;

    List<Action.Result> m_actionResults;

    public Step(Flow.Node node, Instant arrivedOn) {
        m_node = node;
        m_arrivedOn = arrivedOn;
        m_actionResults = new ArrayList<>();
    }

    public Flow.Node getNode() {
        return m_node;
    }

    public Instant getArrivedOn() {
        return m_arrivedOn;
    }

    public Instant getLeftOn() {
        return m_leftOn;
    }

    public void setLeftOn(Instant leftOn) {
        m_leftOn = leftOn;
    }

    public Rule.Result getRuleResult() {
        return m_ruleResult;
    }

    public void setRuleResult(Rule.Result ruleResult) {
        m_ruleResult = ruleResult;
    }

    public List<Action.Result> getActionResults() {
        return m_actionResults;
    }
}
