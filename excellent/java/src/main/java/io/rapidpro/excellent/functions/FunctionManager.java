package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.functions.annotations.BooleanDefault;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the loaded function libraries
 */
public class FunctionManager {

    private Map<String, Method> m_functions = new HashMap<>();

    /**
     * Adds functions from a library class
     * @param library the library class
     */
    public void addLibrary(Class<?> library) {
        for (Method method : library.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                continue; // ignore non-public methods
            }

            String name = method.getName().toLowerCase();

            // strip preceding _ chars used to avoid conflicts with Java keywords
            if (name.startsWith("_")) {
                name = name.substring(1);
            }

            m_functions.put(name, method);
        }
    }

    public Method getFunction(String name) {
        return m_functions.get(name.toLowerCase());
    }

    /**
     * Invokes a function in library
     * @param args the arguments to be passed to the function
     * @return the the function return value
     */
    public Object invokeFunction(String name, List<Object> args) {
        // find function with given name
        Method func = getFunction(name);
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
}
