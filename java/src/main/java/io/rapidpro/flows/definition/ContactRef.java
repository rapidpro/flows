package io.rapidpro.flows.definition;

import com.google.gson.annotations.SerializedName;

/**
 * A reference to a contact which is an object like {"id":123,"name":"Mr Test"}
 */
public class ContactRef {

    @SerializedName("id")
    protected Integer m_id;

    @SerializedName("name")
    protected String m_name;

    public ContactRef() {
    }

    public ContactRef(Integer id, String name) {
        m_id = id;
        m_name = name;
    }

    public Integer getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }
}
