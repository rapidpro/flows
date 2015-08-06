package io.rapidpro.excellent;

import io.rapidpro.excellent.parser.TemplateEvaluatorImpl;

/**
 * Public interface for the Excellent templating system
 */
public class Excellent {

    /**
     * Gets a template evaluator instance
     * @return the instance
     */
    public static TemplateEvaluator getTemplateEvaluator() {
        return new TemplateEvaluatorImpl();
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
