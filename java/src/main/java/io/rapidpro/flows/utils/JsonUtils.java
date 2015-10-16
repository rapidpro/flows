package io.rapidpro.flows.utils;

import com.google.gson.*;
import io.rapidpro.flows.definition.Flow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON utility methods
 */
public class JsonUtils {

    protected static GsonBuilder s_gsonBuilder = new GsonBuilder()
            .registerTypeAdapter(Flow.class, new Flow.Deserializer());

    protected static Gson s_gson = s_gsonBuilder.create();

    public static GsonBuilder getGsonBuilder() {
        return s_gsonBuilder;
    }

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
     * Convenience constructor to create a new JsonObject from pairs of property names and values
     * @param nameValuePairs the name value pairs
     * @return the object
     */
    public static JsonObject object(Object... nameValuePairs) {
        JsonObject obj = new JsonObject();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            String name = (String) nameValuePairs[i];
            Object value = nameValuePairs[i + 1];
            if (value instanceof String) {
                obj.addProperty(name, (String) value);
            } else if (value instanceof Boolean) {
                obj.addProperty(name, (Boolean) value);
            } else if (value instanceof Number) {
                obj.addProperty(name, (Number) value);
            } else if (value instanceof JsonElement) {
                obj.add(name, (JsonElement) value);
            } else if (value == null) {
                obj.add(name, null);
            } else {
                throw new RuntimeException("Can't add value of type " + value.getClass().getSimpleName() + " to JSON");
            }
        }
        return obj;
    }

    public static JsonElement toJson(Object value) {
        if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        } else if (value instanceof Jsonizable) {
            return ((Jsonizable) value).toJson();
        } else {
            throw new RuntimeException("Can't add value of type " + value.getClass().getSimpleName() + " to JSON");
        }
    }

    /**
     * Convenience constructor to create a JSON array from multiple items
     * @param items the array items
     * @return the array
     */
    public static JsonArray array(JsonElement... items) {
        JsonArray arr = new JsonArray();
        for (JsonElement elm : items) {
            arr.add(elm);
        }
        return arr;
    }

    public static JsonArray toJsonArray(List<?> items) {
        JsonArray arr = new JsonArray();
        for (Object item : items) {
            arr.add(toJson(item));
        }
        return arr;
    }

    /**
     * Instantiates a new object instance by calling a static fromJson method on its class.
     * @param elm the JSON element passed to fromJson
     * @param context the deserialization context
     * @param clazz the class to instantiate
     * @return the new object instance
     */
    public static <T> T fromJson(JsonElement elm, Flow.DeserializationContext context, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (T) elm.getAsString();
        } else if (clazz.equals(Boolean.class)) {
            return (T) (Boolean) elm.getAsBoolean();
        }

        try {
            Method method = clazz.getDeclaredMethod("fromJson", JsonElement.class, Flow.DeserializationContext.class);
            return (T) method.invoke(null, elm, context);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(JsonObject obj, String memberName, Flow.DeserializationContext context, Class<T> clazz) {
        JsonElement member = obj.get(memberName);
        if (member == null || member.isJsonNull()) {
            return null;
        } else {
            return fromJson(member, context, clazz);
        }
    }

    public static <T> List<T> fromJsonArray(JsonArray array, Flow.DeserializationContext context, Class<T> clazz) {
        List<T> items = new ArrayList<>();
        for (JsonElement elm : array) {
            items.add(fromJson(elm, context, clazz));
        }
        return items;
    }
}
