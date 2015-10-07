package io.rapidpro.flows.definition;

import com.google.gson.annotations.SerializedName;

/**
 * A variable reference to a contact group (by name) or a contact (by phone)
 */
public class VariableRef {

    protected static final String NEW_CONTACT = "@new_contact";

    @SerializedName("id")
    protected String m_value;

    public VariableRef() {
    }

    public VariableRef(String value) {
        m_value = value;
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
