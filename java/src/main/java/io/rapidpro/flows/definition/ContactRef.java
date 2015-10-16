package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

/**
 * A reference to a contact which is an object like {"id":123,"name":"Mr Test"}
 */
public class ContactRef implements Jsonizable {

    protected Integer m_id;

    protected String m_name;

    public ContactRef(String name) {
        m_name = name;
    }

    public ContactRef(Integer id, String name) {
        m_id = id;
        m_name = name;
    }

    public static ContactRef fromJson(JsonElement elm, Flow.DeserializationContext context) {
        if (elm.isJsonObject()) {
            JsonObject obj = elm.getAsJsonObject();
            return new ContactRef(obj.get("id").getAsInt(), obj.get("name").getAsString());
        } else {
            return new ContactRef(elm.getAsString());
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

    public Integer getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }
}
