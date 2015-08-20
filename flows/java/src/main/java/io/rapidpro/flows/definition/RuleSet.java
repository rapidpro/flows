package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
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

    public static RuleSet fromJson(JsonObject json, Map<Flow.ConnectionStart, String> destinationsToSet) throws JsonSyntaxException {
        RuleSet obj = new RuleSet();
        obj.m_uuid = json.get("uuid").getAsString();
        obj.m_type = Type.valueOf(json.get("ruleset_type").getAsString().toUpperCase());
        obj.m_operand = json.get("operand").getAsString();

        for (JsonElement ruleElem : json.get("rules").getAsJsonArray()) {
            obj.m_rules.add(Rule.fromJson(ruleElem.getAsJsonObject(), destinationsToSet));
        }
        return obj;
    }

    @Override
    public Flow.Node visit(RunState run, Step step, String input) {
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

    protected Pair<Rule, String> findMatchingRule(RunState run, String input) {
        EvaluationContext context = run.buildContext();

        // TODO use operand
        String operand = run.substituteVariables(m_operand, context).getOutput();

        for (Rule rule : m_rules) {
            Test.Result result = rule.matches(run, context, input);
            if (result.getValue() > 0) {
                return new ImmutablePair<>(rule, result.getMatch());
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
