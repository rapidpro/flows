package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.MessageAction;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.utils.JsonUtils;

import java.util.*;

/**
 * A flow definition, typically loaded from JSON
 */
public class Flow {

    // supported versions of the flow spec
    public static Set<Integer> SPEC_VERSIONS = new HashSet<>(Arrays.asList(7, 8, 9, 10));

    public enum Type {
        FLOW("F"),
        MESSAGE("M"),
        VOICE("V"),
        SURVEY("S");

        String m_code;

        Type(String code) {
            m_code = code;
        }

        static Type fromCode(String code) {
            for (Type type : Type.values()) {
                if (type.m_code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    protected Type m_type;

    protected String m_baseLanguage;

    protected Set<String> m_languages;

    protected Node m_entry;

    protected Map<String, Element> m_elementsByUuid = new HashMap<>();

    protected JsonObject m_metadata;

    /**
     * Creates a flow from a JSON flow definition
     * @param json the JSON
     * @return the flow
     */
    public static Flow fromJson(String json) throws FlowParseException {
        JsonObject obj = JsonUtils.getGson().fromJson(json, JsonObject.class);

        if (obj.has("version")) {
            int version = obj.get("version").getAsInt();
            if (!SPEC_VERSIONS.contains(version)) {
                throw new FlowParseException("Unsupported flow spec version: " + version);
            }
        } else {
            throw new FlowParseException("Missing flow spec version");
        }

        Flow flow = new Flow();
        flow.m_type = Flow.Type.fromCode(obj.get("flow_type").getAsString());
        flow.m_baseLanguage = JsonUtils.getAsString(obj, "base_language");
        flow.m_metadata = obj.getAsJsonObject("metadata");

        // keep an exhaustive record of all languages in our flow definition
        Set<String> languages = new HashSet<>();

        DeserializationContext context = new DeserializationContext(new HashMap<String, Flow>());

        for (JsonElement asElem : obj.get("action_sets").getAsJsonArray()) {
            ActionSet actionSet = ActionSet.fromJson(asElem.getAsJsonObject(), context);
            flow.m_elementsByUuid.put(actionSet.m_uuid, actionSet);

            // see what translations are set on this actionset
            for (Action action : actionSet.getActions()) {
                if (action instanceof MessageAction) {
                    languages.addAll(((MessageAction) action).getMsg().getLanguages());
                }
            }
        }

        for (JsonElement rsElem : obj.get("rule_sets").getAsJsonArray()) {
            RuleSet ruleSet = RuleSet.fromJson(rsElem.getAsJsonObject(), context);
            flow.m_elementsByUuid.put(ruleSet.m_uuid, ruleSet);

            for (Rule rule : ruleSet.getRules()) {
                flow.m_elementsByUuid.put(rule.getUuid(), rule);
                languages.addAll(rule.getCategory().getLanguages());
            }
        }

        // lookup and set destination nodes
        for (Map.Entry<ConnectionStart, String> entry : context.m_destinationsToSet.entrySet()) {
            ConnectionStart start = entry.getKey();
            start.setDestination((Node) flow.getElementByUuid(entry.getValue()));
        }

        // only accept languages that are ISO 639-2 (alpha3)
        flow.m_languages = new HashSet<>();
        for (String language : languages) {
            if (language.length() == 3) {
                flow.m_languages.add(language);
            }
        }

        flow.m_entry = flow.getElementByUuid(JsonUtils.getAsString(obj, "entry"));
        return flow;
    }

    /**
     * Allows state to be provided to deserialization methods
     */
    public static class DeserializationContext {

        protected Map<String,Flow> m_flows;

        protected Map<ConnectionStart, String> m_destinationsToSet = new HashMap<>();

        public DeserializationContext(Map<String,Flow> flows) {
            m_flows = flows;
        }

        public DeserializationContext(Flow flow) {
            m_flows = new HashMap<>();
            m_flows.put(flow.getUuid(), flow);
        }

        public void needsDestination(ConnectionStart start, String destinationUuid) {
            m_destinationsToSet.put(start, destinationUuid);
        }

        public Flow getFlow(String flowUuid) {
            return m_flows.get(flowUuid);
        }
    }

    /**
     * Super class of anything in a flow definition with a UUID
     */
    public static abstract class Element {

        protected String m_uuid;

        public String getUuid() {
            return m_uuid;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Element)) return false;

            Element element = (Element) o;

            return m_uuid.equals(element.m_uuid);
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return m_uuid.hashCode();
        }
    }

    /**
     * Super class for ActionSet and RuleSet. Things which can be a destination in a flow graph.
     */
    public static abstract class Node extends Element {

        /**
         * Visits this node
         * @param runner the flow runner
         * @param run the run state
         * @param step the current step
         * @param input the last input
         * @return the next destination (may be null)
         */
        public abstract Node visit(Runner runner, RunState run, Step step, Input input);

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "Node{uuid=\"" + m_uuid + "\"}";
        }
    }

    /**
     * ActionSets and Rules can have destinations which are serialized as the node UUID
     */
    public interface ConnectionStart {
        Node getDestination();

        void setDestination(Node destination);
    }

    public Type getType() {
        return m_type;
    }

    /**
     * Gets all the languages present in the flow definition
     */
    public Set<String> getLanguages() {
        return m_languages;
    }

    public String getBaseLanguage() {
        return m_baseLanguage;
    }

    public Node getEntry() {
        return m_entry;
    }

    public JsonObject getMetadata() {
        return m_metadata;
    }

    public String getUuid() {
        return m_metadata.get("uuid").getAsString();
    }

    public <T extends Element> T getElementByUuid(String uuid) {
        return (T) m_elementsByUuid.get(uuid);
    }
}
