package io.rapidpro.excellent;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The evaluation context, i.e. the data constants accessible in an expression
 */
public class EvaluationContext extends HashMap<String, Object> {

    private static Gson m_gson;
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(EvaluationContext.class, new ContextDeserializer());
        m_gson = builder.create();
    }

    public static EvaluationContext fromJson(String json) {
        return m_gson.fromJson(json, EvaluationContext.class);
    }

    /**
     * Returns a named item from the context, e.g. contact, contact.name
     */
    public Object read(String path) {
        return readFromContainer(this, path.toLowerCase(), path);
    }

    private Object readFromContainer(Map<String, Object> container, String path, String originalPath) {
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

            return readFromContainer((Map<String, Object>)value, remainingPath, originalPath);
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

    public String toJson() {
        return m_gson.toJson(this);
    }

    /**
     * JSON de-serializer for context objects
     */
    private static class ContextDeserializer implements JsonDeserializer<EvaluationContext> {
        @Override
        public EvaluationContext deserialize(JsonElement node, Type type, JsonDeserializationContext context) throws JsonParseException {
            EvaluationContext obj = new EvaluationContext();

            for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
                obj.put(entry.getKey(), handleNode(entry.getValue(), Object.class, context));
            }
            return obj;
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
