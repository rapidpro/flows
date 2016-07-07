package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        WAIT_GPS,
        WAIT_PHOTO,
        WAIT_VIDEO,
        WAIT_AUDIO,
        WEBHOOK,
        FLOW_FIELD,
        FORM_FIELD,
        CONTACT_FIELD,
        EXPRESSION,
        SUBFLOW
    }

    protected Type m_rulesetType;

    protected String m_label;

    protected String m_operand;

    protected Map<String, Object> m_config;

    protected List<Rule> m_rules = new ArrayList<>();

    /**
     * If this rule is a subflow
     */
    public boolean isSubflow() {
        return m_rulesetType.equals(Type.SUBFLOW);
    }

    /**
     * Get the id for our subflow if we have one
     */
    public String getSubflowUUID() {
        Object flow = m_config.get("flow");
        if (flow != null) {
            return ((Map)flow).get("uuid").toString();
        }
        return null;
    }

    /**
     * Creates a rule set from a JSON object
     * @param obj the JSON object
     * @param context the deserialization context
     * @return the rule set
     */
    public static RuleSet fromJson(JsonObject obj, Flow.DeserializationContext context) throws FlowParseException {
        RuleSet set = new RuleSet();
        set.m_uuid = obj.get("uuid").getAsString();
        set.m_rulesetType = Type.valueOf(obj.get("ruleset_type").getAsString().toUpperCase());
        set.m_label = obj.get("label").getAsString();
        set.m_operand = obj.get("operand").getAsString();
        set.m_config = JsonUtils.getGson().fromJson(obj.get("config"), Map.class);

        for (JsonElement ruleElem : obj.get("rules").getAsJsonArray()) {
            set.m_rules.add(Rule.fromJson(ruleElem.getAsJsonObject(), context));
        }
        return set;
    }

    /**
     * @see io.rapidpro.flows.definition.Flow.Node#visit(Runner, RunState, Step, Input)
     */
    @Override
    public Flow.Node visit(Runner runner, RunState run, Step step, Input input) {
        if (logger.isDebugEnabled()) {
            logger.debug("Visiting rule set " + m_uuid + " with input " + input + " from contact " + run.getContact().getUuid());
        }

        input.consume();

        EvaluationContext context = run.buildContext(runner, input);

        Pair<Rule, Test.Result> match = findMatchingRule(runner, run, context);
        if (match == null) {
            return null;
        }

        Rule rule = match.getLeft();
        Test.Result testResult = match.getRight();

        // get category in the flow base language
        String category = rule.getCategory().getLocalized(Collections.singletonList(run.getFlow().getBaseLanguage()), "");

        String valueAsStr = Conversions.toString(testResult.getValue(), context);
        Result result = new Result(rule, valueAsStr, category, input.getValueAsText(context), input.getMedia(), step.getFlow().getUUID());
        step.setRuleResult(result);

        run.updateValue(this, result, input.getTime());

        return rule.getDestination();
    }

    /**
     * Runs through the rules to find the first one that matches
     * @param runner the flow runner
     * @param run the current run state
     * @param context the evaluation context
     * @return the matching rule and the test result
     */
    protected Pair<Rule, Test.Result> findMatchingRule(Runner runner, RunState run, EvaluationContext context) {
        String operand;

        // for form fields, construct operand as field expression
        if (m_rulesetType == RuleSet.Type.FORM_FIELD) {
            String fieldDelimiter = getConfigAsString("field_delimiter", " ");
            int fieldIndex = getConfigAsInt("field_index", 0) + 1;
            operand = "@(FIELD(" + m_operand.substring(1) + ", " + fieldIndex + ", \"" + fieldDelimiter + "\"))";
        } else {
            operand = m_operand;
        }

        operand = runner.substituteVariables(operand, context).getOutput();

        for (Rule rule : m_rules) {
            Test.Result result = rule.matches(runner, run, context, operand);
            if (result.isMatched()) {
                return new ImmutablePair<>(rule, result);
            }
        }
        return null;
    }

    public Type getRuleSetType() {
        return m_rulesetType;
    }

    public String getLabel() {
        return m_label;
    }

    public String getOperand() {
        return m_operand;
    }

    public List<Rule> getRules() {
        return m_rules;
    }

    public boolean isPause() {
        return m_rulesetType == Type.WAIT_MESSAGE
                || m_rulesetType == Type.WAIT_DIGIT
                || m_rulesetType == Type.WAIT_DIGITS
                || m_rulesetType == Type.WAIT_AUDIO
                || m_rulesetType == Type.WAIT_GPS
                || m_rulesetType == Type.WAIT_PHOTO
                || m_rulesetType == Type.WAIT_VIDEO;
    }

    protected String getConfigAsString(String key, String defaultValue) {
        return (String) ExpressionUtils.getOrDefault(m_config, key, defaultValue);
    }

    /**
     * GSON deserializes any numbers in the config as doubles
     */
    protected int getConfigAsInt(String key, int defaultValue) {
        return ((Double) ExpressionUtils.getOrDefault(m_config, key, defaultValue)).intValue();
    }

    /**
     * Holds the result of a ruleset evaluation
     */
    public static class Result implements Jsonizable {

        protected Rule m_rule;

        protected String m_value;

        protected String m_category;

        protected String m_text;

        protected String m_media;

        protected String m_flowUUID;

        public Result(Rule rule, String value, String category, String text, String media, String flowUUID) {
            m_rule = rule;
            m_value = value;
            m_category = category;
            m_text = text;
            m_media = media;
            m_flowUUID = flowUUID;
        }

        public static Result fromJson(JsonElement elm, Flow.DeserializationContext context) {
            JsonObject obj = elm.getAsJsonObject();
            String flowUUID = JsonUtils.getAsString(obj, "flow_uuid");
            Flow flow = context.getFlow(flowUUID);
            return new Result(
                    (Rule) flow.getElementByUuid(obj.get("uuid").getAsString()),
                    JsonUtils.getAsString(obj, "value"),
                    JsonUtils.getAsString(obj, "category"),
                    JsonUtils.getAsString(obj, "text"),
                    JsonUtils.getAsString(obj, "media"),
                    flowUUID
            );
        }

        @Override
        public JsonElement toJson() {
            return JsonUtils.object(
                    "uuid", m_rule.getUuid(),
                    "value", m_value,
                    "category", m_category,
                    "text", m_text,
                    "media", m_media,
                    "flow_uuid", m_flowUUID
            );
        }

        /**
         * Media is mutable since they can point to files that have been moved
         */
        public void setMedia(String media) {
            m_media = media;
        }

        public Rule getRule() {
            return m_rule;
        }

        public String getValue() {
            return m_value;
        }

        public String getCategory() {
            return m_category;
        }

        public String getText() {
            return m_text;
        }

        public String getMedia() {
            return m_media;
        }

    }
}
