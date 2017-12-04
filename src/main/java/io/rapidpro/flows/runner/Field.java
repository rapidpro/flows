package io.rapidpro.flows.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.utils.JsonUtils;
import io.rapidpro.flows.utils.Jsonizable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A contact field
 */
public class Field implements Jsonizable {

    // can't create contact fields with these keys
    protected static Set<String> RESERVED_KEYS = new HashSet<>(Arrays.asList(
            "name", "first_name", "phone", "language", "created_by", "modified_by", "org", "uuid", "groups"));

    public enum ValueType {
        TEXT("T"),
        DECIMAL("N"),
        DATETIME("D"),
        STATE("S"),
        DISTRICT("I"),
        WARD("W");

        String m_code;

        ValueType(String code) {
            m_code = code;
        }

        static ValueType fromCode(String code) {
            for (ValueType type : ValueType.values()) {
                if (type.m_code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    protected String m_key;

    protected String m_label;

    protected ValueType m_valueType;

    protected boolean m_new = false;

    public Field(String key, String label, ValueType valueType) {
        this(key, label, valueType, false);
    }

    public Field(String key, String label, ValueType valueType, boolean isNew) {
        if (!isValidKey(key)) {
            throw new RuntimeException("Field key '" + key + "' is invalid or reserved");
        }
        if (!isValidLabel(label)) {
            throw new RuntimeException("Field label '" + label + "' is invalid or reserved");
        }

        m_key = key;
        m_label = label;
        m_valueType = valueType;
        m_new = isNew;
    }

    public static Field fromJson(JsonElement elm) {
        JsonObject obj = elm.getAsJsonObject();
        return new Field(
                obj.get("key").getAsString(),
                obj.get("label").getAsString(),
                ValueType.fromCode(obj.get("value_type").getAsString())
        );
    }

    @Override
    public JsonElement toJson() {
        return JsonUtils.object(
                "key", m_key,
                "label", m_label,
                "value_type", m_valueType.m_code);
    }

    public static String makeKey(String label) {
        String key = label.toLowerCase().replaceAll("([^a-z0-9]+)", " ").trim();
        return key.replaceAll("([^a-z0-9]+)", "_");
    }

    public static boolean isValidKey(String key) {
        return Pattern.compile("^[a-z][a-z0-9_]*$").matcher(key).matches() && !RESERVED_KEYS.contains(key);
    }

    public static boolean isValidLabel(String label) {
        return Pattern.compile("^[A-Za-z0-9- ]+$").matcher(label).matches();
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
                "m_key=\"" + m_key + "\"" +
                ", m_label=\"" + m_label + "\"" +
                ", m_valueType=" + m_valueType +
                "}";
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
