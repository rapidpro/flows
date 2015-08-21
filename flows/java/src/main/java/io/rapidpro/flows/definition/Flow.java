package io.rapidpro.flows.definition;

import com.google.gson.*;
import io.rapidpro.flows.runner.Input;
import io.rapidpro.flows.runner.Step;
import io.rapidpro.flows.runner.RunState;

import java.lang.reflect.Type;
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

    protected String m_baseLanguage;
    protected Node m_entry;
    protected Map<String, Node> m_nodesByUuid = new HashMap<>();

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

        public Flow deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject obj = elem.getAsJsonObject().get("flow").getAsJsonObject();

                Flow definition = new Flow();
                definition.m_baseLanguage = obj.get("base_language").getAsString();

                Map<ConnectionStart, String> destinationsToSet = new HashMap<>();

                for (JsonElement asElem : obj.get("action_sets").getAsJsonArray()) {
                    ActionSet actionSet = ActionSet.fromJson(asElem.getAsJsonObject(), destinationsToSet);
                    definition.m_nodesByUuid.put(actionSet.m_uuid, actionSet);
                }

                for (JsonElement rsElem : obj.get("rule_sets").getAsJsonArray()) {
                    RuleSet ruleSet = RuleSet.fromJson(rsElem.getAsJsonObject(), destinationsToSet);
                    definition.m_nodesByUuid.put(ruleSet.m_uuid, ruleSet);
                }

                // lookup and set destination nodes
                for (Map.Entry<ConnectionStart, String> entry : destinationsToSet.entrySet()) {
                    ConnectionStart start = entry.getKey();
                    start.setDestination(definition.m_nodesByUuid.get(entry.getValue()));
                }

                definition.m_entry = definition.m_nodesByUuid.get(obj.get("entry").getAsString());

                return definition;
            }
            catch (FlowParseException ex) {
                throw new JsonParseException(ex);
            }
        }
    }

    /**
     * Super class for ActionSet and RuleSet. Things which can be a destination in a flow graph.
     */
    public static abstract class Node {

        protected String m_uuid;

        /**
         * Visits this node
         * @param run the run state
         * @param step the current step
         * @param input the last input
         * @return the next destination (may be null)
         */
        public abstract Node visit(RunState run, Step step, Input input);

        public String getUuid() {
            return m_uuid;
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "Node{uuid=\"" + m_uuid + "\"}";
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return m_uuid.equals(node.m_uuid);

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
     * ActionSets and Rules can have destinations which are serialized as the node UUID
     */
    public interface ConnectionStart {
        Node getDestination();

        void setDestination(Node destination);
    }
}
