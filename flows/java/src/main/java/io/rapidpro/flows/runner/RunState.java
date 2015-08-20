package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluatedTemplate;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.Expressions;
import io.rapidpro.flows.definition.Flow;

import java.util.*;

/**
 *
 */
public class RunState {

    protected static Expressions.TemplateEvaluator s_evaluator = Expressions.getTemplateEvaluator();

    @SerializedName("org")
    protected Org m_org;

    @SerializedName("contact")
    protected Contact m_contact;

    @SerializedName("flow")
    protected Flow m_flow;

    @SerializedName("steps")
    protected LinkedList<Step> m_steps;

    public RunState(Org org, Contact contact, Flow flow) {
        m_org = org;
        m_contact = contact;
        m_flow = flow;
        m_steps = new LinkedList<>();
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

        // TODO
        // variables.put("flow", m_stepState.buildContext(m_org));

        return new EvaluationContext(variables, m_org.getTimezone(), m_org.isDayFirst());
    }
}
