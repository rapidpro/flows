package io.rapidpro.flows.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.rapidpro.flows.definition.Flow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JSON utility methods
 */
public class JsonUtils {

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
     * Instantiates a new object instance by calling a static fromJson method on its class.
     * @param obj the JSON object passed to fromJson
     * @param context the deserialization context
     * @param clazz the class to instantiate
     * @return the new object instance
     */
    public static <T> T fromJson(JsonObject obj, Flow.DeserializationContext context, Class<T> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("fromJson", JsonObject.class, Flow.DeserializationContext.class);
            return (T) method.invoke(null, obj, context);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
