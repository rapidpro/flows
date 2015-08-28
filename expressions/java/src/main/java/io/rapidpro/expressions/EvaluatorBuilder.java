package io.rapidpro.expressions;

import io.rapidpro.expressions.evaluator.TemplateEvaluator;
import io.rapidpro.expressions.functions.CustomFunctions;
import io.rapidpro.expressions.functions.ExcelFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for template evaluators
 */
public class EvaluatorBuilder {

    protected char m_expressionPrefix = '@';

    protected List<Class<?>> m_functionLibraries = new ArrayList<>();

    public EvaluatorBuilder() {
        m_functionLibraries.add(ExcelFunctions.class);
        m_functionLibraries.add(CustomFunctions.class);
    }

    public EvaluatorBuilder withExpressionPrefix(char expressionPrefix) {
        m_expressionPrefix = expressionPrefix;
        return this;
    }

    public EvaluatorBuilder addFunctionLibrary(Class<?> functionLibrary) {
        m_functionLibraries.add(functionLibrary);
        return this;
    }

    public TemplateEvaluator build() {
        return new TemplateEvaluator(m_expressionPrefix, m_functionLibraries);
    }
}
