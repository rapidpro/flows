package io.rapidpro.flows.definition;

import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.flows.FlowUtils;
import io.rapidpro.flows.definition.tests.Test;
import io.rapidpro.flows.runner.RunState;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * A matchable rule in a rule set
 */
public class Rule implements Flow.ConnectionStart {

    protected String m_uuid;

    protected Test m_test;

    protected TranslatableText m_category;

    protected Flow.Node m_destination;

    public static Rule fromJson(JsonObject obj, Map<Flow.ConnectionStart, String> destinationsToSet) throws FlowParseException {
        Rule rule = new Rule();
        rule.m_uuid = obj.get("uuid").getAsString();
        rule.m_test = Test.fromJson(obj.get("test").getAsJsonObject());
        rule.m_category = TranslatableText.fromJson(obj.get("category"));

        String destinationUuid = FlowUtils.getAsString(obj, "destination");
        if (StringUtils.isNotEmpty(destinationUuid)) {
            destinationsToSet.put(rule, destinationUuid);
        }
        return rule;
    }

    public Test.Result matches(RunState run, EvaluationContext context, String input) {
        return m_test.evaluate(run, context, input);
    }

    public String getUuid() {
        return m_uuid;
    }

    public Test getTest() {
        return m_test;
    }

    public TranslatableText getCategory() {
        return m_category;
    }

    @Override
    public Flow.Node getDestination() {
        return m_destination;
    }

    @Override
    public void setDestination(Flow.Node destination) {
        this.m_destination = destination;
    }

    /**
     * Holds the result of the matched rule
     */
    public static class Result {

        protected Rule m_rule;

        protected String m_value;

        protected String m_category;

        public Result(Rule rule, String value, String category) {
            m_rule = rule;
            m_value = value;
            m_category = category;
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
    }
}
