package io.rapidpro.flows.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.rapidpro.expressions.utils.ExpressionUtils;
import io.rapidpro.flows.definition.Flow;
import io.rapidpro.flows.definition.GroupRef;
import io.rapidpro.flows.definition.LabelRef;
import io.rapidpro.flows.definition.actions.Action;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON utility methods
 */
public class JsonUtils {

    protected static GsonBuilder s_gsonBuilder = new GsonBuilder()
            .registerTypeAdapter(Flow.class, new Flow.Deserializer())
            .registerTypeAdapter(Action.class, new Action.Serializer())
            .registerTypeAdapter(Action.class, new Action.Deserializer())
            .registerTypeAdapter(LabelRef.class, new LabelRef.Serializer())
            .registerTypeAdapter(LabelRef.class, new LabelRef.Deserializer())
            .registerTypeAdapter(GroupRef.class, new GroupRef.Serializer())
            .registerTypeAdapter(GroupRef.class, new GroupRef.Deserializer());

    protected static Gson s_gson = s_gsonBuilder.create();

    protected static ThreadLocal<Flow.DeserializationContext> s_deserializationContext = new ThreadLocal<>();

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
            } else {
                throw new RuntimeException("Can't add value of type " + value.getClass().getSimpleName() + " to JSON");
            }
        }
        return obj;
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

    public static JsonElement[] eachToJson(Iterable<? extends Jsonizable> items) {
        List<JsonElement> res = new ArrayList<>();
        for (Jsonizable item : items) {
            res.add(item.toJson());
        }
        return res.toArray(new JsonElement[res.size()]);
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

    /**
     * Adapter for ZoneId instances to serialize as id string, e.g. "Africa/Kigali"
     */
    public static class TimezoneAdapter extends TypeAdapter<ZoneId> {
        @Override
        public void write(JsonWriter out, ZoneId zoneId) throws IOException {
            out.value(zoneId.getId());
        }

        @Override
        public ZoneId read(JsonReader in) throws IOException {
            return ZoneId.of(in.nextString());
        }
    }

    /**
     * Adapter for Instant instances to serialize as ISO8601 in UTC, with millisecond precision,
     * e.g. "2014-10-03T01:41:12.790Z"
     */
    public static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant instant) throws IOException {
            if (instant != null) {
                out.value(ExpressionUtils.formatJsonDate(instant));
            } else {
                out.nullValue();
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            return ExpressionUtils.parseJsonDate(in.nextString());
        }
    }

    public static Flow.DeserializationContext getDeserializationContext() {
        return s_deserializationContext.get();
    }

    public static void setDeserializationContext(Flow.DeserializationContext context) {
        s_deserializationContext.set(context);
    }

    public static void clearDeserializationContext() {
        s_deserializationContext.remove();
    }
}
