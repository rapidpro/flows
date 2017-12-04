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

    protected String m_uuid;

    protected String m_name;
    private String uuid;

    public ContactRef(String name) {
        m_name = name;
    }

    public ContactRef(String uuid, String name) {
        m_uuid = uuid;
        m_name = name;
    }

    public static ContactRef fromJson(JsonElement elm, Flow.DeserializationContext context) {
        if (elm.isJsonObject()) {
            JsonObject obj = elm.getAsJsonObject();
            return new ContactRef(obj.get("uuid").getAsString(), obj.get("name").getAsString());
        } else {
            return new ContactRef(elm.getAsString());
        }
    }

    @Override
    public JsonElement toJson() {
        if (m_uuid != null) {
            return JsonUtils.object("uuid", m_uuid, "name", m_name);
        } else {
            return new JsonPrimitive(m_name);
        }
    }

    public String getUuid() {
        return m_uuid;
    }

    public String getName() {
        return m_name;
    }
}
