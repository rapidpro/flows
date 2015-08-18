package io.rapidpro.expressions;

import io.rapidpro.expressions.evaluator.TemplateEvaluatorImpl;

/**
 * Public interface for the Expressions templating system
 */
public class Expressions {

    private static TemplateEvaluator s_evaluator = new TemplateEvaluatorImpl();

    /**
     * Gets a template evaluator instance
     * @return the instance
     */
    public static TemplateEvaluator getTemplateEvaluator() {
        return s_evaluator;
    }

    /**
     * The template evaluator public interface
     */
    public interface TemplateEvaluator {
        /**
         * Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
         * @param template the template string
         * @param context the evaluation context
         * @return a tuple of the evaluated template and a list of evaluation errors
         */
        EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context);

        /**
         * Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
         * @param template the template string
         * @param context the evaluation context
         * @param urlEncode whether or not values should be URL encoded
         * @return a tuple of the evaluated template and a list of evaluation errors
         */
        EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context, boolean urlEncode);

        /**
         * Evaluates a single expression, e.g. "contact.reports * 2"
         * @param expression the expression string
         * @param context the evaluation context
         * @return the evaluated expression value
         * @throws EvaluationError if an error occurs during evaluation
         */
        Object evaluateExpression(String expression, EvaluationContext context) throws EvaluationError;
    }
}
