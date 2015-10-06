package io.rapidpro.flows.runner;

import com.google.gson.annotations.SerializedName;

/**
 * A contact field
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

    protected boolean m_new = false;

    public Field() {
    }

    public Field(String key, String label, ValueType valueType) {
        m_key = key;
        m_label = label;
        m_valueType = valueType;
        m_new = true;
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

    public boolean isNew() {
        return m_new;
    }

    @Override
    public String toString() {
        return "Field{" +
                "m_key='" + m_key + '\'' +
                ", m_label='" + m_label + '\'' +
                ", m_valueType=" + m_valueType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (!m_key.equals(field.m_key)) return false;
        if (!m_label.equals(field.m_label)) return false;
        return m_valueType == field.m_valueType;

    }

    @Override
    public int hashCode() {
        int result = m_key.hashCode();
        result = 31 * result + m_label.hashCode();
        result = 31 * result + m_valueType.hashCode();
        return result;
    }
}
