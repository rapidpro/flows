package io.rapidpro.excellent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods
 */
public class EvaluationUtils {

    public static Object invokeFunction(String name, List<Object> args) {
        // find function with given name
        Method func = null;
        for (Method method : Functions.class.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("_")) {
                methodName = methodName.substring(1);
            }

            if (methodName.equalsIgnoreCase(name)) {
                func = method;
                break;
            }
        }

        if (func == null) {
            throw new EvaluationError("No such function " + name);
        }

        List<Object> parameters = new ArrayList<>();
        for (Parameter param : func.getParameters()) {
            DefaultParam defaultParam = param.getAnnotation(DefaultParam.class);

            if (param.getType().isArray()) { // we've reach a varargs param
                parameters.add(args.toArray(new Object[args.size()]));
                args.clear();
                break;
            }
            else if (args.size() > 0) {
                Object arg = args.remove(0);
                parameters.add(arg);
            }
            else if (defaultParam != null) {
                parameters.add(defaultParam.value());
            }
            else {
                throw new EvaluationError("Missing argument " + param.getName() + " for function " + name);
            }
        }

        if (!args.isEmpty()) {
            throw new EvaluationError("Too many arguments provided for function " + name);
        }

        try {
            return func.invoke(null, parameters.toArray(new Object[parameters.size()]));
        } catch (IllegalAccessException | InvocationTargetException e) {
            // TODO format pretty arg list
            throw new EvaluationError("Error calling function '" + name + "' with arguments ???", e);
        }
    }
}
