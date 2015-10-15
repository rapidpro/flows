package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import io.rapidpro.flows.utils.FlowUtils;
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

    @SerializedName("fields")
    protected List<Field> m_fields;

    @SerializedName("contact")
    protected Contact m_contact;

    @SerializedName("started")
    protected Instant m_started;

    @SerializedName("steps")
    protected List<Step> m_steps;

    @SerializedName("values")
    protected Map<String, Value> m_values;

    @SerializedName("extra")
    protected Map<String, Object> m_extra;

    @SerializedName("state")
    protected State m_state;

    protected transient Flow m_flow;

    protected RunState() {
    }

    /**
     * Creates a run state for a new run by the given contact in the given flow
     * @param org the org
     * @param fields the contact fields
     * @param contact the contact
     * @param flow the flow
     */
    public RunState(Org org, List<Field> fields, Contact contact, Flow flow) {
        this.m_org = org;
        this.m_fields = fields;
        this.m_contact = contact;
        this.m_started = Instant.now();
        this.m_steps = new ArrayList<>();
        this.m_values = new HashMap<>();
        this.m_extra = new HashMap<>();
        this.m_state = State.IN_PROGRESS;
        this.m_flow = flow;
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
    public EvaluationContext buildContext(Runner runner, Input input) {
        // our concept of now may be overridden by the runner
        Instant now = runner.getNow() != null ? runner.getNow() : Instant.now();

        EvaluationContext context = new EvaluationContext(new HashMap<String, Object>(), m_org.getTimezone(), m_org.getDateStyle(), now);

        Map<String, String> contactContext = m_contact.buildContext(this, context);

        if (input != null) {
            context.putVariable("step", input.buildContext(context, contactContext));
        }

        context.putVariable("date", buildDateContext(context));
        context.putVariable("contact", contactContext);
        context.putVariable("extra", m_extra);

        Map<String, Object> flowContext = new HashMap<>();
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, Value> entry : m_values.entrySet()) {
            flowContext.put(entry.getKey(), entry.getValue().buildContext(context));
            values.add(entry.getKey() + ": " + entry.getValue().getValue());
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
    public void updateValue(RuleSet ruleSet, RuleSet.Result result, Instant time) {
        String key = ruleSet.getLabel().toLowerCase().replaceAll("[^a-z0-9]+", "_");

        m_values.put(key, new Value(result.getValue(), result.getCategory(), result.getText(), time));
    }

    /**
     * Builds the date context (i.e. @date.now, @date.today, ...)
     */
    protected static Map<String, String> buildDateContext(EvaluationContext container) {
        ZonedDateTime asDateTime = container.getNow().atZone(container.getTimezone());
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

    public Field getOrCreateField(String key) {
        return getOrCreateField(key, null, Field.ValueType.TEXT);
    }

    public Field getOrCreateField(String key, String label) {
        return getOrCreateField(key, label, Field.ValueType.TEXT);
    }

    public Field getOrCreateField(String key, String label, Field.ValueType valueType) {
        if (key == null && label == null) {
            throw new RuntimeException("Must provide either key or label");
        }

        if (key != null) {
            // TODO get this into a map for efficiency
            for (Field field : m_fields) {
                if (field.getKey().equals(key)) {
                    return field;
                }
            }
        } else {
            key = Field.makeKey(label);
        }

        if (label == null) {
            label = FlowUtils.title(key.replaceAll("([^A-Za-z0-9- ]+)", " "));
        }

        Field field = new Field(key, label, valueType);
        m_fields.add(field);
        return field;
    }

    public List<Field> getCreatedFields() {
        List<Field> created = new ArrayList<>();
        for (Field field : m_fields) {
            if (field.isNew()) {
                created.add(field);
            }
        }
        return created;
    }

    public Contact getContact() {
        return m_contact;
    }

    public Flow getFlow() {
        return m_flow;
    }

    public Instant getStarted() {
        return m_started;
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

    public Map<String, Object> getExtra() {
        return m_extra;
    }

    public State getState() {
        return m_state;
    }

    public void setState(State state) {
        m_state = state;
    }
}
