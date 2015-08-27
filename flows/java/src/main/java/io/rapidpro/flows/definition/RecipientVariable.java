package io.rapidpro.flows.definition;

import com.google.gson.annotations.SerializedName;

/**
 * A variable reference to a contact group (by name) or a contact (by phone)
 */
public class RecipientVariable {

    protected static final String NEW_CONTACT = "@new_contact";

    @SerializedName("id")
    protected String m_id;

    public String getId() {
        return m_id;
    }

    public RecipientVariable() {
    }

    public RecipientVariable(String id) {
        m_id = id;
    }

    boolean isNewContact() {
        return m_id.equalsIgnoreCase(NEW_CONTACT);
    }
}
