package io.rapidpro.flows.utils;

import com.google.gson.*;
import io.rapidpro.flows.definition.Flow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * JSON utility methods
 */
public class JsonUtils {

    protected static Gson s_gson = new GsonBuilder().create();

    public static Gson getGson() {
        return s_gson;
    }

    /**
     * Gets the named member as a string, returning null if it's null of it doesn't exist
     * @param obj the parsed JSON object
     * @param memberName the object member name
     * @return the string value or null
     */
    public static String getAsString(JsonObject obj, String memberName) {
        JsonElement member = obj.get(memberName);
        return (member == null || member.isJsonNull()) ? null : member.getAsString();
    }

    /**
     * Gets the named member as an integer, returning null if it's null of it doesn't exist
     * @param obj the parsed JSON object
     * @param memberName the object member name
     * @return the integer value or null
     */
    public static Integer getAsInteger(JsonObject obj, String memberName) {
        JsonElement member = obj.get(memberName);
        return (member == null || member.isJsonNull()) ? null : member.getAsInt();
    }

    /**
     * Tries to convert a value to a JSON element
     * @param value the value to convert
     * @return the JSON element
     */
    public static JsonElement toJson(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        } else if (value instanceof JsonElement) {
            return (JsonElement) value;
        } else if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        } else if (value instanceof Jsonizable) {
            return ((Jsonizable) value).toJson();
        } else {
            throw new RuntimeException("Can't convert value of type " + value.getClass().getSimpleName() + " to JSON");
        }
    }

    /**
     * Converts a map to a JSON object
     * @param map the map to convert
     * @return the JSON object
     */
    public static JsonObject toJsonObject(Map<String, ?> map) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            obj.add(entry.getKey(), toJson(entry.getValue()));
        }
        return obj;
    }

    /**
     * Converts an iterable of items to a JSON array
     * @param items the iterable to convert
     * @return the JSON array
     */
    public static JsonArray toJsonArray(Iterable<?> items) {
        JsonArray arr = new JsonArray();
        for (Object item : items) {
            arr.add(toJson(item));
        }
        return arr;
    }

    /**
     * Convenience constructor to create a JSON object from pairs of property names and values
     * @param nameValuePairs the name value pairs
     * @return the object
     */
    public static JsonObject object(Object... nameValuePairs) {
        JsonObject obj = new JsonObject();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            String name = (String) nameValuePairs[i];
            Object value = nameValuePairs[i + 1];
            obj.add(name, toJson(value));
        }
        return obj;
    }

    /**
     * Convenience constructor to create a JSON array from multiple items
     * @param items the array items
     * @return the array
     */
    public static JsonArray array(Object... items) {
        return toJsonArray(Arrays.asList(items));
    }

    /**
     * Loads an object from JSON. If object is not a primitive, it's class must declare a fromJson method.
     * @param elm the JSON element
     * @param context the deserialization context (may be null)
     * @param clazz the class to instantiate
     * @return the new object instance
     */
    public static <T> T fromJson(JsonElement elm, Flow.DeserializationContext context, Class<T> clazz) {
        if (elm == null || elm.isJsonNull()) {
            return null;
        }

        if (clazz.equals(String.class)) {
            return (T) elm.getAsString();
        } else if (clazz.equals(Boolean.class)) {
            return (T) (Boolean) elm.getAsBoolean();
        } else if (clazz.equals(Integer.class)) {
            return (T) (Integer) elm.getAsInt();
        }

        try {
            if (context != null) {
                Method method = clazz.getDeclaredMethod("fromJson", JsonElement.class, Flow.DeserializationContext.class);
                return (T) method.invoke(null, elm, context);
            } else {
                Method method = clazz.getDeclaredMethod("fromJson", JsonElement.class);
                return (T) method.invoke(null, elm);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> fromJsonArray(JsonArray arr, Flow.DeserializationContext context, Class<T> clazz) {
        List<T> items = new ArrayList<>();
        for (JsonElement elm : arr) {
            items.add(fromJson(elm, context, clazz));
        }
        return items;
    }

    public static <V> Map<String, V> fromJsonObject(JsonObject obj, Flow.DeserializationContext context, Class<V> clazz) {
        Map<String, V> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            map.put(entry.getKey(), fromJson(entry.getValue(), context, clazz));
        }
        return map;
    }

    public static <V> List<Map<String, V>> fromJsonObjectArray(JsonArray arr, Flow.DeserializationContext context, Class<V> clazz) {
        List<Map<String, V>> items = new ArrayList<>();
        for (JsonElement elm : arr) {
            items.add(fromJsonObject(elm.getAsJsonObject(), context, clazz));
        }
        return items;
    }

}
