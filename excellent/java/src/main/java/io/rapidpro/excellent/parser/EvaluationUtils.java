package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.functions.annotations.BooleanDefault;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods
 */
public class EvaluationUtils {

    /**
     * Invokes a function in library
     * @param library the class containing function definitions
     * @param name the name of function (case insensitive)
     * @param args the arguments to be passed to the function
     * @return the the function return value
     */
    public static Object invokeFunction(Class<?> library, String name, List<Object> args) {
        // find function with given name
        Method func = null;
        for (Method method : library.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                continue; // ignore non-public methods
            }

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
            IntegerDefault defaultInt = param.getAnnotation(IntegerDefault.class);
            BooleanDefault defaultBool = param.getAnnotation(BooleanDefault.class);

            if (param.getType().isArray()) { // we've reach a varargs param
                parameters.add(args.toArray(new Object[args.size()]));
                args.clear();
                break;
            }
            else if (args.size() > 0) {
                Object arg = args.remove(0);
                parameters.add(arg);
            }
            else if (defaultInt != null) {
                parameters.add(defaultInt.value());
            }
            else if (defaultBool != null) {
                parameters.add(defaultBool.value());
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

    /**
     * Slices a list, Python style
     * @param list the list
     * @param start the start index (null means the beginning of the list)
     * @param stop the stop index (null means the end of the list)
     * @return the slice
     */
    public static <T> List<T> slice(List<T> list, Integer start, Integer stop) {
        int size = list.size();

        if (start == null) {
            start = 0;
        } else if (start < 0) {
            start = size + start;
        }

        if (stop == null) {
            stop = size;
        } else if (stop < 0) {
            stop = size + stop;
        }

        if (start >= size || stop <= 0 || start >= stop) {
            return Collections.emptyList();
        }

        start = Math.max(0, start);
        stop = Math.min(size, stop);

        return list.subList(start, stop);
    }

    /**
     * Pow for two decimals
     */
    public static BigDecimal pow(BigDecimal number, BigDecimal power) {
        return new BigDecimal(Math.pow(number.doubleValue(), power.doubleValue()));
    }
}
