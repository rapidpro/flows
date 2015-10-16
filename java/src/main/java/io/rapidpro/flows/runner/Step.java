package io.rapidpro.flows.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * A step taken by a contact or surveyor in a flow run
 */
public class Step implements Jsonizable {

    protected Flow.Node m_node;

    protected Instant m_arrivedOn;

    protected Instant m_leftOn;

    protected RuleSet.Result m_ruleResult;

    protected List<Action> m_actions;

    protected List<String> m_errors;

    public Step(Flow.Node node, Instant arrivedOn) {
        m_node = node;
        m_arrivedOn = arrivedOn;
        m_actions = new ArrayList<>();
        m_errors = new ArrayList<>();
    }

    public Step(Flow.Node node, Instant arrivedOn, Instant leftOn, RuleSet.Result ruleResult, List<Action> actions, List<String> errors) {
        m_node = node;
        m_arrivedOn = arrivedOn;
        m_leftOn = leftOn;
        m_ruleResult = ruleResult;
        m_actions = actions;
        m_errors = errors;
    }

    public static Step fromJson(JsonElement elm, Flow.DeserializationContext context) {
        JsonObject obj = elm.getAsJsonObject();
        return new Step(
                (Flow.Node) context.getFlow().getElementByUuid(obj.get("node").getAsString()),
                ExpressionUtils.parseJsonDate(JsonUtils.getAsString(obj, "arrived_on")),
                ExpressionUtils.parseJsonDate(JsonUtils.getAsString(obj, "left_on")),
                JsonUtils.fromJson(obj, "rule", context, RuleSet.Result.class),
                JsonUtils.fromJsonArray(obj.get("actions").getAsJsonArray(), context, Action.class),
                JsonUtils.fromJsonArray(obj.get("errors").getAsJsonArray(), context, String.class)
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "node", m_node.getUuid(),
                "arrived_on", ExpressionUtils.formatJsonDate(m_arrivedOn),
                "left_on", ExpressionUtils.formatJsonDate(m_leftOn),
                "rule", m_ruleResult != null ? m_ruleResult.toJson() : null,
                "actions", JsonUtils.toJsonArray(m_actions),
                "errors", JsonUtils.toJsonArray(m_errors)
        );
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

    public RuleSet.Result getRuleResult() {
        return m_ruleResult;
    }

    public void setRuleResult(RuleSet.Result ruleResult) {
        m_ruleResult = ruleResult;
    }

    public List<Action> getActions() {
        return m_actions;
    }

    public void addActionResult(Action.Result actionResult) {
        if (actionResult.getPerformed() != null) {
            m_actions.add(actionResult.getPerformed());
        }
        if (actionResult.hasErrors()) {
            m_errors.addAll(actionResult.getErrors());
        }
    }

    public List<String> getErrors() {
        return m_errors;
    }

    public boolean isCompleted() {
        return m_leftOn != null;
    }
}
