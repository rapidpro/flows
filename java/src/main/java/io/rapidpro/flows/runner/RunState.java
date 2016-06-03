package io.rapidpro.flows.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.expressions.EvaluationContext;
import io.rapidpro.expressions.evaluator.Conversions;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.RuleSet;
import io.rapidpro.flows.utils.FlowUtils;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.*;

/**
 * Represents state of a flow run after visiting one or more nodes in the flow
 */
public class RunState implements Jsonizable {



    public enum State {
        IN_PROGRESS,
        COMPLETED,
        WAIT_AUDIO,
        WAIT_MESSAGE,
        WAIT_PHOTO,
        WAIT_GPS,
        WAIT_VIDEO
    }

    protected Org m_org;

    protected List<Field> m_fields;

    protected Contact m_contact;

    protected Instant m_started;

    protected List<Step> m_steps;

    protected List<Map<String, Value>> m_values;

    protected Map<String, String> m_extra;

    protected State m_state;

    protected Map<Integer,Flow> m_flows;

    protected List<Integer> m_activeFlowIds;

    protected List<Step> m_suspendedSteps;

    protected int m_level;

    /**
     * Creates a run state for a new run by the given contact in the given flow
     * @param org the org
     * @param fields the contact fields
     * @param contact the contact
     */
    public RunState(Org org, List<Field> fields, Contact contact, Map<Integer,Flow> flows) {
        this.m_org = org;
        this.m_fields = fields;
        this.m_contact = contact;
        this.m_started = Instant.now();
        this.m_steps = new ArrayList<>();
        this.m_extra = new HashMap<>();
        this.m_state = State.IN_PROGRESS;
        this.m_flows = flows;
        this.m_level = 0;
        this.m_activeFlowIds = new ArrayList<>();
        this.m_suspendedSteps = new ArrayList<>();

        // our current level of values
        this.m_values = new ArrayList<>();
        this.m_values.add(new HashMap<String, Value>());
    }

    public static Map<Integer,Flow> buildFlowMap(Flow flow) {
        Map<Integer,Flow> flows = new HashMap<>();
        flows.put(flow.getId(), flow);
        return flows;
    }

    /**
     * Restores a run state from JSON
     * @param json the JSON containing a serialized run state
     * @param flows the flows the run state is for
     * @return the run state
     */
    public static RunState fromJson(String json, Map<Integer,Flow> flows) {
        JsonObject obj = JsonUtils.getGson().fromJson(json, JsonObject.class);
        Flow.DeserializationContext context = new Flow.DeserializationContext(flows);

        RunState run = new RunState(
                Org.fromJson(obj.get("org")),
                JsonUtils.fromJsonArray(obj.get("fields").getAsJsonArray(), null, Field.class),
                Contact.fromJson(obj.get("contact")),
                flows
        );

        run.m_started = ExpressionUtils.parseJsonDate(JsonUtils.getAsString(obj, "started"));
        run.m_steps = JsonUtils.fromJsonArray(obj.get("steps").getAsJsonArray(), context, Step.class);
        run.m_values = JsonUtils.fromJsonObjectArray(obj.get("values").getAsJsonArray(), null, Value.class);
        run.m_extra = JsonUtils.fromJsonObject(obj.get("extra").getAsJsonObject(), null, String.class);
        run.m_state = State.valueOf(obj.get("state").getAsString().toUpperCase());
        run.m_level = obj.get("level").getAsInt();
        run.m_suspendedSteps = JsonUtils.fromJsonArray(obj.get("suspended_steps").getAsJsonArray(), context, Step.class);
        run.m_activeFlowIds = JsonUtils.fromJsonArray(obj.get("active_flows").getAsJsonArray(), null, Integer.class);

        return run;
    }

    /**
     * Serializes this run state to JSON
     * @return the JSON
     */
    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "org", m_org.toJson(),
                "fields", JsonUtils.toJsonArray(m_fields),
                "contact", m_contact.toJson(),
                "started", ExpressionUtils.formatJsonDate(m_started),
                "steps", JsonUtils.toJsonArray(m_steps),
                "values", toJsonObjectArray(m_values),
                "extra", JsonUtils.toJsonObject(m_extra),
                "state", m_state.name().toLowerCase(),
                "active_flows", JsonUtils.toJsonArray(m_activeFlowIds),
                "suspended_steps", JsonUtils.toJsonArray(m_suspendedSteps),
                "level", m_level
        );
    }

    /**
     * Helper to write out our list of value maps as JSON
     */
    private JsonArray toJsonObjectArray(Iterable<Map<String, Value>> values) {
        JsonArray arr = new JsonArray();
        for (Map<String, ?> item : values) {
            arr.add(JsonUtils.toJsonObject(item));
        }
        return arr;
    }

    /**
     * Serializes this run state to a JSON string
     * @return the JSON
     */
    public String toJsonString() {
        return JsonUtils.getGson().toJson(toJson());
    }

    /**
     * Sets the active flow by pushing on to our list of flows
     * @param activeFlow
     */
    public void setActiveFlow(int activeFlow) {
        this.m_activeFlowIds.add(activeFlow);
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
        context.putVariable("flow", buildFlowContext(getValues(), context));

        // add the flow that was one level above us
        if (m_level > 0) {
            context.putVariable("parent", buildFlowContext(m_values.get(m_level - 1), context));
        }

        // if we have a child below us, add that context in too
        if (m_values.size() > m_level + 1) {
            context.putVariable("child", buildFlowContext(m_values.get(m_level + 1), context));
        }

        return context;
    }

    /**
     * Builds up a flow context from a set of flow values
     */
    private Map<String,Object> buildFlowContext(Map<String,Value> flowValues, EvaluationContext context) {
        Map<String, Object> flowContext = new HashMap<>();
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, Value> entry : flowValues.entrySet()) {
            flowContext.put(entry.getKey(), entry.getValue().buildContext(context));
            values.add(entry.getKey() + ": " + entry.getValue().getValue());
        }
        flowContext.put("*", StringUtils.join(values, "\n"));
        return flowContext;
    }

    /**
     * Updates a value in response to a rule match
     * @param ruleSet the rule set
     * @param result the rule match result
     * @param time the time from the input
     */
    public void updateValue(RuleSet ruleSet, RuleSet.Result result, Instant time) {
        String key = ruleSet.getLabel().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        getValues().put(key, new Value(result.getValue(), result.getCategory(), result.getText(), time));
    }

    /**
     * Enters a subflow. Suspends the current step and pushes the provided
     * flow on to our active flow list.
     * @param currentStep the step to suspend until completion of the subflow
     * @param flowId the flow start
     */
    public void enterSubflow(Step currentStep, int flowId) {
        m_level++;
        m_suspendedSteps.add(currentStep);
        setActiveFlow(flowId);

        // wipe any existing values at our new level
        getValues().clear();

    }

    /**
     * Exits our current subflow, going back to the parent.
     * @return the most recently suspended step
     */
    public Step exitSubflow() {
        m_level--;
        m_activeFlowIds.remove(m_activeFlowIds.size() - 1);
        return m_suspendedSteps.remove(m_suspendedSteps.size() - 1);
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

        Field field = new Field(key, label, valueType, true);
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
        return m_flows.get(getActiveFlowId());
    }

    public int getActiveFlowId() {
        return m_activeFlowIds.get(m_activeFlowIds.size() - 1);
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

    public void clearCompletedSteps() {
        List<Step> incomplete = new ArrayList<>();
        for (Step step : m_steps) {
            if (!step.isCompleted() && m_state != State.COMPLETED) {
                incomplete.add(step);
            }
        }
        m_steps = incomplete;
    }


    /**
     * Get the values at the current flow level
     */
    public Map<String, Value> getValues() {
        while (m_values.size() <= m_level) {
            m_values.add(new HashMap<String, Value>());
        }
        return m_values.get(m_level);
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
