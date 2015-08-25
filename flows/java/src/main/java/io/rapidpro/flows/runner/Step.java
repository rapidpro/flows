package io.rapidpro.flows.runner;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.Rule;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.utils.JsonUtils;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Step {

    @SerializedName("node")
    @JsonAdapter(Flow.Element.RefAdapter.class)
    protected Flow.Node m_node;

    @SerializedName("arrived_on")
    @JsonAdapter(JsonUtils.InstantAdapter.class)
    protected Instant m_arrivedOn;

    @SerializedName("left_on")
    @JsonAdapter(JsonUtils.InstantAdapter.class)
    protected Instant m_leftOn;

    @SerializedName("rule")
    protected Rule.Result m_ruleResult;

    @SerializedName("actions")
    List<Action> m_actions;

    @SerializedName("errors")
    List<String> m_errors;

    public Step(Flow.Node node, Instant arrivedOn) {
        m_node = node;
        m_arrivedOn = arrivedOn;
        m_actions = new ArrayList<>();
        m_errors = new ArrayList<>();
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

    public List<Action> getActions() {
        return m_actions;
    }

    public void addActionResult(Action.Result actionResult) {
        if (actionResult.getAction() != null) {
            m_actions.add(actionResult.getAction());
        }
        if (actionResult.hasErrors()) {
            m_errors.addAll(actionResult.getErrors());
        }
    }

    public List<String> getErrors() {
        return m_errors;
    }
}
