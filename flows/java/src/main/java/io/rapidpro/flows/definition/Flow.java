package io.rapidpro.flows.definition;

import com.google.gson.*;
import io.rapidpro.flows.runner.Contact;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
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

    public static Flow fromJson(String json) {
        return s_gson.fromJson(json, Flow.class);
    }

    public String getBaseLanguage() {
        return m_baseLanguage;
    }

    public Node getEntry() {
        return m_entry;
    }

    public String getLocalizedText(TranslatableText text, Contact contact) {
        return getLocalizedText(text, contact, "");
    }

    public String getLocalizedText(TranslatableText text, Contact contact, String defaultText) {
        // We return according to the following precedence:
        //   1) Contact's language
        //   2) Org Primary Language
        //   3) Flow Base Language
        //   4) Default Text
        List<String> preferredLanguages = new ArrayList<>();

        if (StringUtils.isNotEmpty(contact.getLanguage())) {
            preferredLanguages.add(contact.getLanguage());
        }

        // TODO add org language somehow

        preferredLanguages.add(m_baseLanguage);

        return text.getLocalized(preferredLanguages, defaultText);
    }

    public static class Deserializer implements JsonDeserializer<Flow> {

        public Flow deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
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
    }

    /**
     * Super class for ActionSet and RuleSet. Things which can be a destination.
     */
    public static class Node {
        protected String m_uuid;
    }

    public interface ConnectionStart {
        void setDestination(Node destination);
    }
}
