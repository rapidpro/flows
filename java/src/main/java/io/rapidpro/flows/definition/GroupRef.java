package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

/**
 * Reference to a contact group which can be an object like {"id":123,"name":"Testers"} or an expression string
 */
public class GroupRef implements Jsonizable {

    protected Integer m_id;

    protected String m_name;

    public Integer getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public GroupRef(String name) {
        m_name = name;
    }

    public GroupRef(Integer id, String name) {
        m_id = id;
        m_name = name;
    }

    public static GroupRef fromJson(JsonElement elm, Flow.DeserializationContext context) {
        if (elm.isJsonObject()) {
            JsonObject obj = elm.getAsJsonObject();
            return new GroupRef(JsonUtils.getAsInteger(obj, "id"), obj.get("name").getAsString());
        } else {
            return new GroupRef(elm.getAsString());
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
        return "GroupRef{id=" + m_id + ", name=\"" + m_name + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupRef ref = (GroupRef) o;

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
