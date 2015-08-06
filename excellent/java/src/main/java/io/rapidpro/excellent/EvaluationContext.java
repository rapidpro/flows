package io.rapidpro.excellent;

import java.util.HashMap;
import java.util.Map;

/**
 * The evaluation context, i.e. the data constants accessible in an expression
 */
public class EvaluationContext extends HashMap<String, Object> {

    public EvaluationContext() {
        // add some built in values
        put("true", true);
        put("false", false);
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
            throw new EvaluationError("No item called '" + originalPath + " in context");
        }
    }
}
