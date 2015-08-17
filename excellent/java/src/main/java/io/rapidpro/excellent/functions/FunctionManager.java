package io.rapidpro.excellent.functions;

import io.rapidpro.excellent.EvaluationContext;
import io.rapidpro.excellent.EvaluationError;
import io.rapidpro.excellent.evaluator.Conversions;
import io.rapidpro.excellent.functions.annotations.BooleanDefault;
import io.rapidpro.excellent.functions.annotations.IntegerDefault;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Invokes a function
     * @param ctx the evaluation context
     * @param name the function name (case insensitive)
     * @param args the arguments to be passed to the function
     * @return the function return value
     */
    public Object invokeFunction(EvaluationContext ctx, String name, List<Object> args) {
        // find function with given name
        Method func = getFunction(name);
        if (func == null) {
            throw new EvaluationError("No such function " + name);
        }

        List<Object> parameters = new ArrayList<>();
        List<Object> remainingArgs = new ArrayList<>(args);

        for (Parameter param : func.getParameters()) {
            IntegerDefault defaultInt = param.getAnnotation(IntegerDefault.class);
            BooleanDefault defaultBool = param.getAnnotation(BooleanDefault.class);

            if (param.getType().equals(EvaluationContext.class)) {
                parameters.add(ctx);
            }
            else if (param.getType().isArray()) {
                // we've reach a varargs param
                parameters.add(remainingArgs.toArray(new Object[args.size()]));
                remainingArgs.clear();
                break;
            }
            else if (remainingArgs.size() > 0) {
                Object arg = remainingArgs.remove(0);
                parameters.add(arg);
            }
            else if (defaultInt != null) {
                parameters.add(defaultInt.value());
            }
            else if (defaultBool != null) {
                parameters.add(defaultBool.value());
            }
            else {
                throw new EvaluationError("Too few arguments provided for function " + name);
            }
        }

        if (!remainingArgs.isEmpty()) {
            throw new EvaluationError("Too many arguments provided for function " + name);
        }

        try {
            return func.invoke(null, parameters.toArray(new Object[parameters.size()]));
        } catch (IllegalAccessException | InvocationTargetException e) {
            List<String> prettyArgs = new ArrayList<>();
            for (Object arg : args) {
                String pretty;
                if (arg instanceof String) {
                    pretty = "\"" + arg + "\"";
                }
                else {
                    try {
                        pretty = Conversions.toString(arg, ctx);
                    }
                    catch (EvaluationError ex) {
                        pretty = arg.toString();
                    }
                }
                prettyArgs.add(pretty);
            }
            throw new EvaluationError("Error calling function " + name + " with arguments " + StringUtils.join(prettyArgs, ", "), e);
        }
    }
}
