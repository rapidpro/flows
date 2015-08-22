package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.Expressions;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.Rule;
import io.rapidpro.flows.definition.RuleSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents state of a flow run after visiting one or more nodes in the flow
 */
public class RunState {

    protected static Expressions.TemplateEvaluator s_evaluator = Expressions.getTemplateEvaluator();

    public enum State {
        @SerializedName("in_progress") IN_PROGRESS,
        @SerializedName("completed") COMPLETED,
        @SerializedName("wait_message") WAIT_MESSAGE
    }

    @SerializedName("org")
    protected Org m_org;

    @SerializedName("contact")
    protected Contact m_contact;

    @SerializedName("flow")
    protected Flow m_flow;

    @SerializedName("steps")
    protected List<Step> m_steps;

    @SerializedName("values")
    protected Map<String, Value> m_values;

    @SerializedName("extra")
    protected Map<String, String> m_extra;

    @SerializedName("state")
    protected State m_state;

    public RunState(Org org, Contact contact, Flow flow) {
        m_org = org;
        m_contact = contact;
        m_flow = flow;
        m_steps = new ArrayList<>();
        m_values = new HashMap<>();
        m_extra = new HashMap<>();
        m_state = State.IN_PROGRESS;
    }

    public Org getOrg() {
        return m_org;
    }

    public Contact getContact() {
        return m_contact;
    }

    public Flow getFlow() {
        return m_flow;
    }

    public List<Step> getSteps() {
        return m_steps;
    }

    public Map<String, Value> getValues() {
        return m_values;
    }

    public Map<String, String> getExtra() {
        return m_extra;
    }

    public State getState() {
        return m_state;
    }

    public void setState(State state) {
        m_state = state;
    }

    public EvaluatedTemplate substituteVariables(String text, EvaluationContext context) {
        // TODO update context when necessary

        return s_evaluator.evaluateTemplate(text, context);
    }

    public EvaluationContext buildContext(Input input) {
        EvaluationContext context = new EvaluationContext(new HashMap<String, Object>(), m_org.getTimezone(), m_org.isDayFirst());

        if (input != null) {
            context.putVariable("step", input.buildContext(m_org));
        }

        // TODO add date values to context

        context.putVariable("contact", m_contact.buildContext(m_org));
        context.putVariable("extra", m_extra);

        Map<String, Object> flowContext = new HashMap<>();
        for (Map.Entry<String, Value> entry : m_values.entrySet()) {
            flowContext.put(entry.getKey(), entry.getValue().buildContext(m_org));
        }
        context.putVariable("flow", flowContext);

        return context;
    }

    /**
     * Updates a value in response to a rule match
     * @param ruleSet the rule set
     * @param result the rule match result
     * @param time the time from the input
     */
    public void updateValue(RuleSet ruleSet, Rule.Result result, Instant time) {
        String key = ruleSet.getLabel().toLowerCase().replaceAll("[^a-z0-9]+", "_");

        m_values.put(key, new Value(result.getValue(), result.getCategory(), result.getText(), time));
    }
}
