package io.rapidpro.excellent;

import com.google.gson.*;
import io.rapidpro.excellent.evaluator.DateParser;
import io.rapidpro.excellent.evaluator.EvaluatorUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * The evaluation context, i.e. date options and the variables accessible in an expression
 */
public class EvaluationContext {

    protected static Gson m_gson;
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(EvaluationContext.class, new Deserializer());
        m_gson = builder.create();
    }

    protected Map<String, Object> m_variables;

    protected ZoneId m_timezone;

    protected boolean m_dayFirst;

    public EvaluationContext() {
        this.m_variables = new HashMap<>();
        this.m_timezone = ZoneOffset.UTC;
        this.m_dayFirst = true;
    }

    public EvaluationContext(Map<String, Object> variables, ZoneId timezone, boolean dayFirst) {
        this.m_variables = variables;
        this.m_timezone = timezone;
        this.m_dayFirst = dayFirst;
    }

    public static EvaluationContext fromJson(String json) {
        return m_gson.fromJson(json, EvaluationContext.class);
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

    public boolean isDayFirst() {
        return m_dayFirst;
    }

    public void setDayFirst(boolean dayFirst) {
        m_dayFirst = dayFirst;
    }

    public DateTimeFormatter getDateFormatter(boolean incTime) {
        return EvaluatorUtils.getDateFormatter(this.m_dayFirst, incTime);
    }

    public DateParser getDateParser() {
        return new DateParser(ZonedDateTime.now(this.m_timezone), m_dayFirst);
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

        Object value = container.getOrDefault(item, null);

        if (remainingPath != null && value != null) {
            if (!(value instanceof Map)) {
                throw new RuntimeException("Context lookup into non-map container");
            }

            return resolveVariableInContainer((Map<String, Object>) value, remainingPath, originalPath);
        }
        else if (value != null) {
            if (value instanceof Map) {
                Map valueAsMap = ((Map) value);
                if (valueAsMap.containsKey("*")) {
                    return valueAsMap.get("*");
                }
                else {
                    throw new RuntimeException("Context contains map without default key");
                }
            } else {
                return value;
            }
        }
        else {
            throw new EvaluationError("No item called " + originalPath + " in context");
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

            Map<String, Object> variables = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : varsObj.entrySet()) {
                variables.put(entry.getKey(), handleNode(entry.getValue(), Object.class, context));
            }

            return new EvaluationContext(variables, timezone, dayFirst);
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
