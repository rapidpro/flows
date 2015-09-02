package io.rapidpro.flows.definition;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.rapidpro.flows.definition.actions.Action;
import io.rapidpro.flows.definition.actions.message.MessageAction;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Runner;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A flow definition, typically loaded from JSON
 */
public class Flow {

    public enum Type {
        MESSAGE,
        IVR,
        SURVEY
    }

    protected Type m_type;

    protected String m_baseLanguage;

    protected Set<String> m_languages;

    protected Node m_entry;

    protected Map<String, Element> m_elementsByUuid = new HashMap<>();

    /**
     * Creates a flow from a JSON flow definition
     * @param json the JSON
     * @return the flow
     */
    public static Flow fromJson(String json) throws FlowParseException {
        return JsonUtils.getGson().fromJson(json, Flow.class);
    }

    /**
     * Custom JSON deserializer
     */
    public static class Deserializer implements JsonDeserializer<Flow> {
        @Override
        public Flow deserialize(JsonElement elem, java.lang.reflect.Type type, JsonDeserializationContext jsonContext) throws JsonParseException {
            JsonObject obj = elem.getAsJsonObject().get("flow").getAsJsonObject();

            Flow flow = new Flow();
            flow.m_type = Type.MESSAGE;  // TODO flow type should be included in the JSON
            flow.m_baseLanguage = obj.get("base_language").getAsString();

            // keep an exhaustive list of all languages in our flow definition
            Set<String> languages = new HashSet<>();

            DeserializationContext context = new DeserializationContext(flow);

            for (JsonElement asElem : obj.get("action_sets").getAsJsonArray()) {
                ActionSet actionSet = ActionSet.fromJson(asElem.getAsJsonObject(), context, jsonContext);

                // see what translations are set on this actionset
                for (Action action : actionSet.getActions()) {
                    if (action instanceof MessageAction) {
                        languages.addAll(((MessageAction)action).getMsg().m_translations.keySet());
                    }
                }

                flow.m_elementsByUuid.put(actionSet.m_uuid, actionSet);
            }

            for (JsonElement rsElem : obj.get("rule_sets").getAsJsonArray()) {
                RuleSet ruleSet = RuleSet.fromJson(rsElem.getAsJsonObject(), context);

                // see what translations are set on this ruleset
                for(Rule rule : ruleSet.getRules()) {
                    languages.addAll(rule.getCategory().m_translations.keySet());
                }
                flow.m_elementsByUuid.put(ruleSet.m_uuid, ruleSet);
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
    }

    /**
     * Allows state to be provided to deserialization methods
     */
    public static class DeserializationContext {

        protected Flow m_flow;

        protected Map<ConnectionStart, String> m_destinationsToSet = new HashMap<>();

        public DeserializationContext(Flow flow) {
            m_flow = flow;
        }

        public void needsDestination(ConnectionStart start, String destinationUuid) {
            m_destinationsToSet.put(start, destinationUuid);
        }

        public Flow getFlow() {
            return m_flow;
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
         * Serializes the element as a reference to its UUID
         */
        public class RefAdapter extends TypeAdapter<Element> {
            @Override
            public void write(JsonWriter out, Element element) throws IOException {
                out.value(element.getUuid());
            }
            @Override
            public Element read(JsonReader in) throws IOException {
                String elementUuid = in.nextString();
                return JsonUtils.getDeserializationContext().getFlow().getElementByUuid(elementUuid);
            }
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

    public <T extends Element> T getElementByUuid(String uuid) {
        return (T) m_elementsByUuid.get(uuid);
    }
}
