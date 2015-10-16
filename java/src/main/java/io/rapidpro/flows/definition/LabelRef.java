package io.rapidpro.flows.definition;

import com.google.gson.*;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

/**
 * Reference to a label which can be an object like {"id":123,"name":"Testing"} or an expression string
 */
public class LabelRef implements Jsonizable {

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

    public static LabelRef fromJson(JsonElement elm, Flow.DeserializationContext context) {
        if (elm.isJsonObject()) {
            JsonObject obj = elm.getAsJsonObject();
            return new LabelRef(obj.get("id").getAsInt(), obj.get("name").getAsString());
        } else {
            return new LabelRef(elm.getAsString());
        }
    }

    @Override
    public JsonElement toJson() {
        if (m_id != null) {
            return JsonUtils.object("id", m_id, "name", m_name);
        } else {
            return new JsonPrimitive(m_name);
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
