package io.rapidpro.excellent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
public class ExcellentUtils {

    public static Object invokeFunction(String name, List<Object> parameters) {
        // find function with given name
        Method func = null;
        for (Method method : Functions.class.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase(name)) {
                func = method;
                break;
            }
        }

        if (func == null) {
            throw new EvaluationError("Undefined function '" + name + "'");
        }

        try {
            Object[] args = parameters.toArray(new Object[parameters.size()]);
            return func.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // TODO format pretty arg list
            throw new EvaluationError("Error calling function '" + name + "' with arguments ???", e);
        }
    }
}
