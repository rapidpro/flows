package io.rapidpro.flows.definition;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Reference to a label which can be an object like {"id":123,"name":"Testing"} or an expression string
 */
public class LabelRef {

    protected Integer m_id;

    protected String m_name;

    public Integer getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public LabelRef(String name) {
        m_name = name;
    }

    public LabelRef(Integer id, String name) {
        m_id = id;
        m_name = name;
    }

    public static class Deserializer implements JsonDeserializer<LabelRef> {
        @Override
        public LabelRef deserialize(JsonElement elem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                return new LabelRef(obj.get("id").getAsInt(), obj.get("name").getAsString());
            } else {
                return new LabelRef(elem.getAsString());
            }
        }
    }

    public static class Serializer implements JsonSerializer<LabelRef> {
        @Override
        public JsonElement serialize(LabelRef group, Type type, JsonSerializationContext context) {
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

    @Override
    public String toString() {
        return "LabelRef{id=" + m_id + ", name=\"" + m_name + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LabelRef ref = (LabelRef) o;

        if (m_id != null ? !m_id.equals(ref.m_id) : ref.m_id != null) return false;
        return m_name.equals(ref.m_name);
    }

    @Override
    public int hashCode() {
        int result = m_id != null ? m_id.hashCode() : 0;
        result = 31 * result + m_name.hashCode();
        return result;
    }
}
