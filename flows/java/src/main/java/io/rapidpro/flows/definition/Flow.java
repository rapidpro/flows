package io.rapidpro.flows.definition;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.RunState;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A flow definition, typically loaded from JSON
 */
public class Flow {

    protected static Gson s_gson = null;
    static {
        s_gson = new GsonBuilder()
                .registerTypeAdapter(Flow.class, new Deserializer())
                .create();
    }

    public enum Type {
        MESSAGE,
        IVR,
        SURVEY
    }

    protected Type m_type;

    protected String m_baseLanguage;

    protected Node m_entry;

    protected Map<String, Node> m_nodesByUuid = new HashMap<>();

    /**
     * Creates a flow from a JSON object
     * @param obj the JSON object
     * @return the flow
     */
    public static Flow fromJson(JsonObject obj) throws FlowParseException {
        Flow flow = new Flow();
        flow.m_type = Type.MESSAGE;  // TODO flow type should be included in the JSON
        flow.m_baseLanguage = obj.get("base_language").getAsString();

        DeserializationContext flowContext = new DeserializationContext(flow);

        for (JsonElement asElem : obj.get("action_sets").getAsJsonArray()) {
            ActionSet actionSet = ActionSet.fromJson(asElem.getAsJsonObject(), flowContext);
            flow.m_nodesByUuid.put(actionSet.m_uuid, actionSet);
        }

        for (JsonElement rsElem : obj.get("rule_sets").getAsJsonArray()) {
            RuleSet ruleSet = RuleSet.fromJson(rsElem.getAsJsonObject(), flowContext);
            flow.m_nodesByUuid.put(ruleSet.m_uuid, ruleSet);
        }

        // lookup and set destination nodes
        for (Map.Entry<ConnectionStart, String> entry : flowContext.m_destinationsToSet.entrySet()) {
            ConnectionStart start = entry.getKey();
            start.setDestination(flow.m_nodesByUuid.get(entry.getValue()));
        }

        flow.m_entry = flow.m_nodesByUuid.get(JsonUtils.getAsString(obj, "entry"));

        return flow;
    }

    /**
     * Creates a flow from a JSON flow definition
     * @param json the JSON
     * @return the flow
     */
    public static Flow fromJson(String json) throws FlowParseException {
        try {
            return s_gson.fromJson(json, Flow.class);
        }
        catch (JsonParseException ex) {
            if (ex.getCause() instanceof FlowParseException) {
                throw (FlowParseException) ex.getCause();
            } else {
                throw ex;
            }
        }
    }

    public String getBaseLanguage() {
        return m_baseLanguage;
    }

    public Node getEntry() {
        return m_entry;
    }

    public static class Deserializer implements JsonDeserializer<Flow> {

        public Flow deserialize(JsonElement elem, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject obj = elem.getAsJsonObject().get("flow").getAsJsonObject();
                return Flow.fromJson(obj);
            }
            catch (FlowParseException ex) {
                throw new JsonParseException(ex);
            }
        }
    }

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
                throw new UnsupportedOperationException();
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
         * @param run the run state
         * @param step the current step
         * @param input the last input
         * @return the next destination (may be null)
         */
        public abstract Node visit(RunState run, Step step, Input input);

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
}
