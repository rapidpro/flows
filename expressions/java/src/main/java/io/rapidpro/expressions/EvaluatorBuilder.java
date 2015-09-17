package io.rapidpro.expressions;

import io.rapidpro.expressions.evaluator.Evaluator;
import io.rapidpro.expressions.functions.CustomFunctions;
import io.rapidpro.expressions.functions.ExcelFunctions;

import java.util.*;

/**
 * Builder for evaluators
 */
public class EvaluatorBuilder {

    protected char m_expressionPrefix = '@';

    protected Set<String> m_allowedTopLevels = new HashSet<>();

    protected List<Class<?>> m_functionLibraries = new ArrayList<>();

    public EvaluatorBuilder() {
        m_functionLibraries.add(ExcelFunctions.class);
        m_functionLibraries.add(CustomFunctions.class);
    }

    public EvaluatorBuilder withExpressionPrefix(char expressionPrefix) {
        m_expressionPrefix = expressionPrefix;
        return this;
    }

    public EvaluatorBuilder withAllowedTopLevels(String[] topLevels) {
        m_allowedTopLevels = new HashSet<>(Arrays.asList(topLevels));
        return this;
    }

    public EvaluatorBuilder addFunctionLibrary(Class<?> functionLibrary) {
        m_functionLibraries.add(functionLibrary);
        return this;
    }

    public Evaluator build() {
        return new Evaluator(m_expressionPrefix, m_allowedTopLevels, m_functionLibraries);
    }
}
