package io.rapidpro.flows.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

/**
 * A variable reference to a contact group (by name) or a contact (by phone)
 */
public class VariableRef implements Jsonizable {

    protected static final String NEW_CONTACT = "@new_contact";

    protected String m_value;

    public VariableRef(String value) {
        m_value = value;
    }

    public static VariableRef fromJson(JsonElement elm, Flow.DeserializationContext context) {
        JsonObject obj = elm.getAsJsonObject();
        return new VariableRef(obj.get("id").getAsString());
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object("id", m_value);
    }

    public String getValue() {
        return m_value;
    }

    /**
     * Returns whether this variable is a placeholder for a new contact
     */
    public boolean isNewContact() {
        return m_value.equalsIgnoreCase(NEW_CONTACT);
    }
}
