package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class Field {

    public enum ValueType {
        @SerializedName("T") TEXT,
        @SerializedName("N") DECIMAL,
        @SerializedName("D") DATETIME,
        @SerializedName("S") STATE,
        @SerializedName("I") DISTRICT
    }

    @SerializedName("key")
    protected String m_key;

    @SerializedName("label")
    protected String m_label;

    @SerializedName("value_type")
    protected ValueType m_valueType;

    public Field() {
    }

    public Field(String key, String label, ValueType valueType) {
        m_key = key;
        m_label = label;
        m_valueType = valueType;
    }

    /**
     * Interface for anything that can provide contact fields
     */
    public interface Provider {
        /**
         * Resolves a field  from the given key
         * @param key the field key
         * @return the field or null if no such field exists
         */
        Field provide(String key);
    }

    public String getKey() {
        return m_key;
    }

    public String getLabel() {
        return m_label;
    }

    public ValueType getValueType() {
        return m_valueType;
    }
}
