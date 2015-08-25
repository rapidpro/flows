package io.rapidpro.expressions;

import com.google.gson.*;
import io.rapidpro.expressions.dates.DateParser;
import io.rapidpro.expressions.dates.DateStyle;
import io.rapidpro.expressions.evaluator.EvaluatorUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The evaluation context, i.e. date options and the variables accessible in an expression
 */
public class EvaluationContext {

    protected static Gson s_gson;
    static {
        s_gson = new GsonBuilder()
                .registerTypeAdapter(EvaluationContext.class, new Deserializer())
                .create();
    }

    protected Map<String, Object> m_variables;

    protected ZoneId m_timezone;

    protected DateStyle m_dateStyle;

    public EvaluationContext() {
        this.m_variables = new HashMap<>();
        this.m_timezone = ZoneOffset.UTC;
        this.m_dateStyle = DateStyle.DAY_FIRST;
    }

    public EvaluationContext(Map<String, Object> variables, ZoneId timezone, DateStyle dateStyle) {
        this.m_variables = variables;
        this.m_timezone = timezone;
        this.m_dateStyle = dateStyle;
    }

    public static EvaluationContext fromJson(String json) {
        return s_gson.fromJson(json, EvaluationContext.class);
    }

    /**
     * Returns a named variable, e.g. contact, contact.name
     */
    public Object resolveVariable(String path) {
        return resolveVariableInContainer(m_variables, path.toLowerCase(), path);
    }

    public void putVariable(String key, Object value) {
        m_variables.put(key.toLowerCase(), value);
    }

    public ZoneId getTimezone() {
        return m_timezone;
    }

    public DateStyle getDateStyle() {
        return m_dateStyle;
    }

    public void setDateStyle(DateStyle dateStyle) {
        m_dateStyle = dateStyle;
    }

    public DateTimeFormatter getDateFormatter(boolean incTime) {
        return EvaluatorUtils.getDateFormatter(m_dateStyle, incTime);
    }

    public DateParser getDateParser() {
        return new DateParser(LocalDate.now(), this.m_timezone, m_dateStyle);
    }

    private Object resolveVariableInContainer(Map<String, Object> container, String path, String originalPath) {
        String item, remainingPath;

        if (path.contains(".")) {
            String[] parts = path.split("\\.", 2);
            item = parts[0];
            remainingPath = parts[1];
        } else {
            item = path;
            remainingPath = null;
        }

        if (!container.containsKey(item)) {
            throw new EvaluationError("No item called " + originalPath + " in context");
        }

        Object value = container.get(item);

        if (remainingPath != null && value != null) {
            if (!(value instanceof Map)) {
                throw new RuntimeException("Context lookup into non-map container");
            }

            return resolveVariableInContainer((Map<String, Object>) value, remainingPath, originalPath);
        }
        else if (value instanceof Map) {
            Map valueAsMap = ((Map) value);
            if (valueAsMap.containsKey("*")) {
                return valueAsMap.get("*");
            }
            else {
                throw new RuntimeException("Context contains map without default key");
            }
        } else if (value != null) {
            return value;
        }
        else {
            return "";  // return empty string rather than null
        }
    }

    /**
     * JSON de-serializer for evaluation contexts
     */
    public static class Deserializer implements JsonDeserializer<EvaluationContext> {
        @Override
        public EvaluationContext deserialize(JsonElement node, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject rootObj = node.getAsJsonObject();
            JsonObject varsObj = rootObj.get("vars").getAsJsonObject();
            ZoneId timezone = ZoneId.of(rootObj.get("tz").getAsString());
            boolean dayFirst = rootObj.get("day_first").getAsBoolean();
            DateStyle dateStyle = dayFirst ? DateStyle.DAY_FIRST : DateStyle.MONTH_FIRST;

            Map<String, Object> variables = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : varsObj.entrySet()) {
                variables.put(entry.getKey(), handleNode(entry.getValue(), Object.class, context));
            }

            return new EvaluationContext(variables, timezone, dateStyle);
        }

        public Object handleNode(JsonElement node, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (node.isJsonNull()) {
                return null;
            }  else if (node.isJsonPrimitive()) {
                return handlePrimitive(node.getAsJsonPrimitive());
            } else if (node.isJsonArray()) {
                return handleArray(node.getAsJsonArray(), context);
            } else {
                return handleObject(node.getAsJsonObject(), context);
            }
        }

        private Object handlePrimitive(JsonPrimitive node) {
            if (node.isBoolean()) {
                return node.getAsBoolean();
            } else if (node.isString()) {
                return node.getAsString();
            } else {
                BigDecimal decimal = node.getAsBigDecimal();
                try {
                    // return numbers as integers if possible
                    return decimal.intValueExact();
                }
                catch (ArithmeticException e) {
                    return decimal;
                }
            }
        }

        private Object handleArray(JsonArray node, JsonDeserializationContext context) {
            Object[] array = new Object[node.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = handleNode(node.get(i), Object.class, context);
            }
            return array;
        }

        private Object handleObject(JsonObject json, JsonDeserializationContext context) {
            Map<String, Object> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                map.put(entry.getKey(), handleNode(entry.getValue(), Object.class, context));
            }
            return map;
        }
    }
}
