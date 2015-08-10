package io.rapidpro.excellent.evaluator;

import io.rapidpro.excellent.*;
import io.rapidpro.excellent.functions.CustomFunctions;
import io.rapidpro.excellent.functions.FunctionManager;
import io.rapidpro.excellent.functions.ExcelFunctions;
import org.abego.treelayout.internal.util.java.lang.string.StringUtil;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the template evaluator
 */
public class TemplateEvaluatorImpl implements Excellent.TemplateEvaluator {

    protected static Logger logger = LoggerFactory.getLogger(TemplateEvaluatorImpl.class);

    private FunctionManager m_functionManager = new FunctionManager();

    public TemplateEvaluatorImpl() {
        m_functionManager.addLibrary(ExcelFunctions.class);
        m_functionManager.addLibrary(CustomFunctions.class);
    }

    /**
     * Templates support 2 forms of embedded expression:
     *  1. Single variable, e.g. @contact, @contact.name (delimited by character type or end of input)
     #  2. Contained expression, e.g. @(SUM(1, 2) + 2) (delimited by balanced parentheses)
     */
    private enum State {
        BODY,               // not in a expression
        PREFIX,             // '=' prefix that denotes the start of an expression
        IDENTIFIER,         // the identifier part, e.g. 'SUM' in '=SUM(1, 2)' or 'contact.age' in '=contact.age'
        BALANCED,           // the balanced parentheses delimited part, e.g. '(1, 2)' in 'SUM(1, 2)'
        STRING_LITERAL      // a string literal
    }

    /**
     * Determines whether the given character is a word character, i.e. \w in a regex
     */
    private static boolean isWordChar(char ch) {
        return Character.isAlphabetic(ch) || ch == '_';
    }

    /**
     * @see io.rapidpro.excellent.Excellent.TemplateEvaluator#evaluateTemplate(String, EvaluationContext)
     */
    @Override
    public EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context) {
        return evaluateTemplate(template, context, false);
    }

    /**
     * @see io.rapidpro.excellent.Excellent.TemplateEvaluator#evaluateTemplate(String, EvaluationContext, boolean)
     */
    @Override
    public EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context, boolean urlEncode) {
        char[] inputChars = template.toCharArray();
        StringBuilder output = new StringBuilder();
        List<String> errors = new ArrayList<>();
        State state = State.BODY;
        StringBuilder currentExpression = new StringBuilder();
        boolean currentExpressionTerminated = false;
        int parenthesesLevel = 0;

        for (int pos = 0; pos < inputChars.length; pos++) {
            char ch = inputChars[pos];

            // in order to determine if the b in a.b terminates an identifier, we have to peek two characters ahead as
            // it could be a.b. (b terminates) or a.b.c (b doesn't terminate)
            char nextCh = (pos < (inputChars.length - 1)) ? inputChars[pos + 1] : 0;
            char nextNextCh = (pos < (inputChars.length - 2)) ? inputChars[pos + 2] : 0;

            if (state == State.BODY) {
                if (ch == '@' && (isWordChar(nextCh) || nextCh == '(')) {
                    state = State.PREFIX;
                    currentExpression = new StringBuilder("" + ch);
                } else {
                    output.append(ch);
                }
            }
            else if (state == State.PREFIX) {
                if (isWordChar(ch)) {
                    state = State.IDENTIFIER; // we're parsing an expression like =XXX
                } else if (ch == '(') {
                    // we're parsing an expression like =(1 + 2)
                    state = State.BALANCED;
                    parenthesesLevel += 1;
                }

                currentExpression.append(ch);
            }
            else if (state == State.IDENTIFIER) {
                currentExpression.append(ch);
            }
            else if (state == State.BALANCED) {
                if (ch == '(') {
                    parenthesesLevel += 1;
                } else if (ch == ')') {
                    parenthesesLevel -= 1;
                } else if (ch == '"') {
                    state = State.STRING_LITERAL;
                }

                currentExpression.append(ch);

                // expression terminates if parentheses balance
                if (parenthesesLevel == 0) {
                    currentExpressionTerminated = true;
                }
            }
            else if (state == State.STRING_LITERAL) {
                if (ch == '"') {
                    state = State.BALANCED;
                }
                currentExpression.append(ch);
            }

            // identifier can terminate expression in 3 ways:
            //  1. next char is null (i.e. end of the input)
            //  2. next char is not a word character or period
            //  3. next char is a period, but it's not followed by a word character
            if (state == State.IDENTIFIER) {
                if (nextCh == 0  || (!isWordChar(nextCh) && nextCh != '.') || (nextCh == '.' && ! isWordChar(nextNextCh))) {
                    currentExpressionTerminated = true;
                }
            }

            if (currentExpressionTerminated) {
                output.append(resolveExpression(currentExpression.toString(), context, urlEncode, errors));
                currentExpression = null;
                currentExpressionTerminated = false;
                state = State.BODY;
            }
        }

        // if last expression didn't terminate - add to output as is
        if (!currentExpressionTerminated && StringUtils.isNotEmpty(currentExpression)) {
            output.append(currentExpression.toString());
        }

        return new EvaluatedTemplate(output.toString(), errors);
    }

    /**
     * Resolves an expression found in the template. If an evaluation error occurs, expression is returned as is.
     */
    private String resolveExpression(String expression, EvaluationContext context, boolean urlEncode, List<String> errors) {
        try {
            String cleaned = expression.substring(1); // strip @ prefix
            Object evaluated = evaluateExpression(cleaned, context);

            String rendered = Conversions.toString(evaluated, context); // render result as string
            return urlEncode ? URLEncoder.encode(rendered, "UTF-8") : rendered;
        }
        catch (EvaluationError ex) {
            logger.debug("Unable to evaluate expression", ex);
            errors.add(ex.getMessage());

            return expression; // if we can't evaluate expression, include it as is in the output
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see io.rapidpro.excellent.Excellent.TemplateEvaluator#evaluateExpression(String, EvaluationContext)
     */
    @Override
    public Object evaluateExpression(String expression, EvaluationContext context) throws EvaluationError {
        ExcellentLexer lexer = new ExcellentLexer(new ANTLRInputStream(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ExcellentParser parser = new ExcellentParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        ParseTree tree;

        try {
            tree = parser.input();

            if (logger.isDebugEnabled()) {
                logger.info("Expression '{}' parsed as {}", expression, tree.toStringTree());
            }
        }
        catch (ParseCancellationException ex) {
            throw new EvaluationError("Expression is invalid", ex);
        }

        ExcellentVisitor visitor = new ExpressionVisitorImpl(m_functionManager, context);
        return visitor.visit(tree);
    }
}
