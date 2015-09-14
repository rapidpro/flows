package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.Rule;
import io.rapidpro.flows.definition.RuleSet;
import io.rapidpro.flows.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents state of a flow run after visiting one or more nodes in the flow
 */
public class RunState {

    public enum State {
        @SerializedName("in_progress") IN_PROGRESS,
        @SerializedName("completed") COMPLETED,
        @SerializedName("wait_message") WAIT_MESSAGE
    }

    @SerializedName("org")
    protected Org m_org;

    @SerializedName("contact")
    protected Contact m_contact;

    @SerializedName("started")
    protected Instant m_started;

    @SerializedName("steps")
    protected List<Step> m_steps;

    @SerializedName("values")
    protected Map<String, Value> m_values;

    @SerializedName("extra")
    protected Map<String, String> m_extra;

    @SerializedName("state")
    protected State m_state;

    protected transient Flow m_flow;

    protected RunState() {
    }

    /**
     * Creates a run state for a new run by the given contact in the given flow
     * @param org the org
     * @param contact the contact
     * @param flow the flow
     * @return the run state
     */
    public static RunState start(Org org, Contact contact, Flow flow) {
        RunState run = new RunState();
        run.m_org = org;
        run.m_contact = contact;
        run.m_started = Instant.now();
        run.m_steps = new ArrayList<>();
        run.m_values = new HashMap<>();
        run.m_extra = new HashMap<>();
        run.m_state = State.IN_PROGRESS;
        run.m_flow = flow;
        return run;
    }

    /**
     * Restores a run state from JSON
     * @param json the JSON containing a serialized run state
     * @param flow the flow the run state is for
     * @return the run state
     */
    public static RunState fromJson(String json, Flow flow) {
        try {
            Flow.DeserializationContext context = new Flow.DeserializationContext(flow);
            JsonUtils.setDeserializationContext(context);

            RunState runState = JsonUtils.getGson().fromJson(json, RunState.class);
            runState.m_flow = flow;

            return runState;
        }
        finally {
            JsonUtils.clearDeserializationContext();
        }
    }

    /**
     * Serializes this run state to JSON
     * @return the JSON
     */
    public String toJson() {
        return JsonUtils.getGson().toJson(this);
    }

    /**
     * Builds the top-level evaluation context (all variables, date information)
     * @param input the current input
     * @return the context
     */
    public EvaluationContext buildContext(Input input) {
        EvaluationContext context = new EvaluationContext(new HashMap<String, Object>(), m_org.getTimezone(), m_org.getDateStyle());

        Map<String, String> contactContext = m_contact.buildContext(m_org);

        if (input != null) {
            context.putVariable("step", input.buildContext(context, contactContext));
        }

        context.putVariable("date", buildDateContext(context, Instant.now()));
        context.putVariable("contact", contactContext);
        context.putVariable("extra", m_extra);

        Map<String, Object> flowContext = new HashMap<>();
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, Value> entry : m_values.entrySet()) {
            flowContext.put(entry.getKey(), entry.getValue().buildContext(context));
            values.add(entry.getKey() + " " + entry.getValue().getValue());
        }
        flowContext.put("*", StringUtils.join(values, "\n"));

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

    /**
     * Builds the date context (i.e. @date.now, @date.today, ...)
     */
    protected static Map<String, String> buildDateContext(EvaluationContext container, Instant now) {
        ZonedDateTime asDateTime = now.atZone(container.getTimezone());
        LocalDate asDate = asDateTime.toLocalDate();

        String asDateTimeStr = Conversions.toString(asDateTime, container);
        String asDateStr = Conversions.toString(asDate, container);

        Map<String, String> dateContext = new HashMap<>();
        dateContext.put("*", asDateTimeStr);
        dateContext.put("now", asDateTimeStr);
        dateContext.put("today", asDateStr);
        dateContext.put("tomorrow", Conversions.toString(asDate.plus(1, ChronoUnit.DAYS), container));
        dateContext.put("yesterday", Conversions.toString(asDate.minus(1, ChronoUnit.DAYS), container));
        return dateContext;
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

    /**
     * Gets the completed steps, i.e. those where the contact left the node or a terminal node
     * @return the completed steps
     */
    public List<Step> getCompletedSteps() {
        List<Step> completed = new ArrayList<>();
        for (Step step : m_steps) {
            if (step.isCompleted() || m_state == State.COMPLETED) {
                completed.add(step);
            }
        }
        return completed;
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
}
