package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Step;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A flow node which is a set of rules, each with its own destination node
 */
public class RuleSet extends Flow.Node {

    protected static Logger logger = LoggerFactory.getLogger(RuleSet.class);

    public enum Type {
        WAIT_MESSAGE,
        WAIT_RECORDING,
        WAIT_DIGIT,
        WAIT_DIGITS,
        WEBHOOK,
        FLOW_FIELD,
        FORM_FIELD,
        CONTACT_FIELD,
        EXPRESSION
    }

    protected Type m_type;

    protected String m_operand;

    protected List<Rule> m_rules = new ArrayList<>();

    /**
     * Creates a rule set from a JSON object
     * @param obj the JSON object
     * @param context the deserialization context
     * @return the rule set
     */
    public static RuleSet fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        RuleSet set = new RuleSet();
        set.m_uuid = obj.get("uuid").getAsString();
        set.m_type = Type.valueOf(obj.get("ruleset_type").getAsString().toUpperCase());
        set.m_operand = obj.get("operand").getAsString();

        for (JsonElement ruleElem : obj.get("rules").getAsJsonArray()) {
            set.m_rules.add(Rule.fromJson(ruleElem.getAsJsonObject(), context));
        }
        return set;
    }

    /**
     * @see io.rapidpro.flows.definition.Flow.Node#visit(RunState, Step, Input)
     */
    @Override
    public Flow.Node visit(RunState run, Step step, Input input) {
        if (logger.isDebugEnabled()) {
            logger.debug("Visiting rule set " + m_uuid + " with input " + input + " from contact " + run.getContact().getUuid());
        }

        Pair<Rule, String> match = findMatchingRule(run, input);
        if (match == null) {
            return null;
        }

        // get category in the flow base language
        Rule rule = match.getLeft();
        String category = rule.getCategory().getLocalized(Collections.singletonList(run.getFlow().getBaseLanguage()), "");

        step.setRuleResult(new Rule.Result(rule, match.getValue(), category));

        return rule.getDestination();
    }

    /**
     * Runs through the rules to find the first one that matches
     * @param run the run state
     * @param input the input
     * @return the rule and the matched text
     */
    protected Pair<Rule, String> findMatchingRule(RunState run, Input input) {
        EvaluationContext context = run.buildContext(input);

        String operand = run.substituteVariables(m_operand, context).getOutput();

        for (Rule rule : m_rules) {
            Test.Result result = rule.matches(run, context, operand);
            if (result.isMatched()) {
                return new ImmutablePair<>(rule, result.getText());
            }
        }
        return null;
    }

    public Type getType() {
        return m_type;
    }

    public String getOperand() {
        return m_operand;
    }

    public List<Rule> getRules() {
        return m_rules;
    }

    public boolean isPause() {
        return m_type == Type.WAIT_MESSAGE || m_type == Type.WAIT_RECORDING || m_type == Type.WAIT_DIGIT || m_type == Type.WAIT_DIGITS;
    }
}
