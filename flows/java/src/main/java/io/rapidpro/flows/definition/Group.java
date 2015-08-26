package io.rapidpro.flows.definition;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Reference to a contact group which can be an object like {"id":123,"name":"Testers"} or an expression string
 */
public class Group {

    protected Integer m_id;

    protected String m_name;

    public Integer getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public Group(String name) {
        m_name = name;
    }

    public Group(Integer id, String name) {
        m_id = id;
        m_name = name;
    }

    public static class Deserializer implements JsonDeserializer<Group> {
        @Override
        public Group deserialize(JsonElement elem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                return new Group(obj.get("id").getAsInt(), obj.get("name").getAsString());
            } else {
                return new Group(elem.getAsString());
            }
        }
    }

    public static class Serializer implements JsonSerializer<Group> {
        @Override
        public JsonElement serialize(Group group, Type type, JsonSerializationContext context) {
            if (group.m_id != null) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", group.m_id);
                obj.addProperty("name", group.m_name);
                return obj;
            }
            else {
                return new JsonPrimitive(group.m_name);
            }
        }
    }
}
