package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.Expressions;
import io.rapidpro.flows.definition.Flow;

import java.util.*;

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
    protected LinkedList<Step> m_steps;

    @SerializedName("extra")
    protected Map<String, String> m_extra;

    @SerializedName("state")
    protected State m_state;

    public RunState(Org org, Contact contact, Flow flow) {
        m_org = org;
        m_contact = contact;
        m_flow = flow;
        m_steps = new LinkedList<>();
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

    public LinkedList<Step> getSteps() {
        return m_steps;
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
        // TODO update context

        return s_evaluator.evaluateTemplate(text, context);
    }

    public EvaluationContext buildContext(Input input) {
        Map<String, Object> variables = new HashMap<>();

        if (input != null) {
            variables.put("step", input.buildContext());
        }

        variables.put("contact", m_contact.buildContext(m_org));
        variables.put("extra", m_extra);

        // TODO add previous rule values as @flow.xxxx
        // variables.put("flow", m_stepState.buildContext(m_org));

        return new EvaluationContext(variables, m_org.getTimezone(), m_org.isDayFirst());
    }
}
