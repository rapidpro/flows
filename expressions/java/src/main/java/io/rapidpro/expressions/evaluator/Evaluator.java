package io.rapidpro.expressions.evaluator;

import io.rapidpro.expressions.*;
import io.rapidpro.expressions.functions.FunctionManager;
import io.rapidpro.expressions.utils.ExpressionUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The template and expression evaluator
 */
public class Evaluator {

    protected static Logger logger = LoggerFactory.getLogger(Evaluator.class);

    private char m_expressionPrefix;

    private FunctionManager m_functionManager = new FunctionManager();

    private Set<String> m_allowedTopLevels;

    public enum EvaluationStrategy {
        COMPLETE,              // evaluate all expressions completely
        RESOLVE_AVAILABLE      // if expression contains missing context references, just substitute what variables we have
    }

    /**
     * Creates a new evaluator
     * @param expressionPrefix the prefix for expressions, e.g. @
     * @param allowedTopLevels top-level context items allowed outside of parentheses, e.g. contact, flow
     * @param functionLibraries the function libraries to include
     */
    public Evaluator(char expressionPrefix, Set<String> allowedTopLevels, List<Class<?>> functionLibraries) {
        m_expressionPrefix = expressionPrefix;
        m_allowedTopLevels = allowedTopLevels;

        for (Class<?> functionLibrary : functionLibraries) {
            m_functionManager.addLibrary(functionLibrary);
        }
    }

    /**
     * Templates support 2 forms of embedded expression:
     *  1. Single variable, e.g. @contact, @contact.name (delimited by character type or end of input)
     #  2. Contained expression, e.g. @(SUM(1, 2) + 2) (delimited by balanced parentheses)
     */
    private enum State {
        BODY,               // not in a expression
        PREFIX,             // '@' prefix that denotes the start of an expression
        IDENTIFIER,         // the identifier part, e.g. 'SUM' in '@SUM(1, 2)' or 'contact.age' in '@contact.age'
        BALANCED,           // the balanced parentheses delimited part, e.g. '(1 + 2)' in '@(1 + 2)'
        STRING_LITERAL,     // a string literal which could contain )
        ESCAPED_PREFIX      // a '@' prefix preceded by another '@'
    }

    /**
     * Determines whether the given character is a word character, i.e. \w in a regex
     */
    private static boolean isWordChar(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_';
    }

    /**
     * Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
     * @param template the template string
     * @param context the evaluation context
     * @return a tuple of the evaluated template and a list of evaluation errors
     */
    public EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context) {
        return evaluateTemplate(template, context, false);
    }

    /**
     * Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
     * @param template the template string
     * @param context the evaluation context
     * @param urlEncode whether or not values should be URL encoded
     * @return a tuple of the evaluated template and a list of evaluation errors
     */
    public EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context, boolean urlEncode) {
        return evaluateTemplate(template, context, urlEncode, EvaluationStrategy.COMPLETE);
    }

    /**
     * Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
     * @param template the template string
     * @param context the evaluation context
     * @param urlEncode whether or not values should be URL encoded
     * @param strategy the evaluation strategy
     * @return a tuple of the evaluated template and a list of evaluation errors
     */
    public EvaluatedTemplate evaluateTemplate(String template, EvaluationContext context, boolean urlEncode, EvaluationStrategy strategy) {
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
                if (ch == m_expressionPrefix && (isWordChar(nextCh) || nextCh == '(')) {
                    state = State.PREFIX;
                    currentExpression = new StringBuilder("" + ch);
                } else if (ch == m_expressionPrefix && nextCh == m_expressionPrefix) {
                    state = State.ESCAPED_PREFIX;
                } else {
                    output.append(ch);
                }
            }
            else if (state == State.PREFIX) {
                if (isWordChar(ch)) {
                    state = State.IDENTIFIER; // we're parsing an expression like @XXX
                } else if (ch == '(') {
                    // we're parsing an expression like @(1 + 2)
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
            else if (state == State.ESCAPED_PREFIX) {
                state = State.BODY;
                output.append(ch);
            }

            // identifier can terminate expression in 3 ways:
            //  1. next char is null (i.e. end of the input)
            //  2. next char is not a word character or period
            //  3. next char is a period, but it's not followed by a word character
            if (state == State.IDENTIFIER) {
                if (nextCh == 0  || (!isWordChar(nextCh) && nextCh != '.') || (nextCh == '.' && !isWordChar(nextNextCh))) {
                    currentExpressionTerminated = true;
                }
            }

            if (currentExpressionTerminated) {
                output.append(resolveExpressionBlock(currentExpression.toString(), context, urlEncode, strategy, errors));
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
     * Resolves an expression block found in the template, e.g. @(...). If an evaluation error occurs, expression is
     * returned as is.
     */
    protected String resolveExpressionBlock(String expression, EvaluationContext context, boolean urlEncode, EvaluationStrategy strategy, List<String> errors) {
        try {
            String body = expression.substring(1); // strip prefix

            // if expression doesn't start with ( then check it's an allowed top level context reference
            if (!body.startsWith("(")) {
                String topLevel = StringUtils.split(body, '.')[0].toLowerCase();
                if (!m_allowedTopLevels.contains(topLevel)) {
                    return expression;
                }
            }

            Object evaluated = evaluateExpression(body, context, strategy);

            String rendered = Conversions.toString(evaluated, context); // render result as string
            return urlEncode ? ExpressionUtils.urlquote(rendered) : rendered;
        }
        catch (EvaluationError ex) {
            logger.debug("Unable to evaluate expression", ex);
            errors.add(ex.getMessage());

            return expression; // if we can't evaluate expression, include it as is in the output
        }
    }

    /**
     * Evaluates a single expression, e.g. "contact.reports * 2"
     * @param expression the expression string
     * @param context the evaluation context
     * @return the evaluated expression value
     * @throws EvaluationError if an error occurs during evaluation
     */
    public Object evaluateExpression(String expression, EvaluationContext context) throws EvaluationError {
        return evaluateExpression(expression, context, EvaluationStrategy.COMPLETE);
    }

    /**
     * Evaluates a single expression, e.g. "contact.reports * 2"
     * @param expression the expression string
     * @param context the evaluation context
     * @param strategy the evaluation strategy
     * @return the evaluated expression value
     * @throws EvaluationError if an error occurs during evaluation
     */
    public Object evaluateExpression(String expression, EvaluationContext context, EvaluationStrategy strategy) throws EvaluationError {
        ExcellentLexer lexer = new ExcellentLexer(new ANTLRInputStream(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ExcellentParser parser = new ExcellentParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        ParseTree tree;

        try {
            tree = parser.parse();

            if (logger.isDebugEnabled()) {
                logger.debug("Expression '{}' parsed as {}", expression, tree.toStringTree());
            }
        }
        catch (ParseCancellationException ex) {
            throw new EvaluationError("Expression is invalid", ex);
        }

        if (strategy == EvaluationStrategy.RESOLVE_AVAILABLE) {
            String resolved = resolveAvailable(tokens, context);
            if (resolved != null) {
                return resolved;
            }
        }

        ExcellentVisitor visitor = new ExpressionVisitorImpl(m_functionManager, context);
        return visitor.visit(tree);
    }

    /**
     * Checks the token stream for context references and if there are missing references - substitutes available
     * references and returns a partially evaluated expression.
     * @param tokens the token stream (all tokens fetched)
     * @param context the evaluation context
     * @return the partially evaluated expression or null if expression can be fully evaluated
     */
    protected String resolveAvailable(CommonTokenStream tokens, EvaluationContext context) {
        boolean hasMissing = false;
        List<Object> outputComponents = new ArrayList<>();

        for (int t = 0; t < tokens.size() - 1; t++) {  // we can ignore the final EOF token
            Token token = tokens.get(t);
            Token nextToken = tokens.get(t + 1);

            // if token is a NAME not followed by ( then it's a context reference
            if (token.getType() == ExcellentParser.NAME && nextToken.getType() != ExcellentParser.LPAREN) {
                try {
                    outputComponents.add(context.resolveVariable(token.getText()));
                } catch (EvaluationError ex) {
                    hasMissing = true;
                    outputComponents.add(token);
                }
            } else {
                outputComponents.add(token);
            }
        }

        // if we don't have missing context references, perform evaluation as normal
        if (!hasMissing) {
            return null;
        }

        // re-combine the tokens and context values back into an expression
        StringBuilder output = new StringBuilder(String.valueOf(m_expressionPrefix));

        for (Object outputComponent : outputComponents) {
            String compVal;
            if (outputComponent instanceof Token) {
                compVal = ((Token) outputComponent).getText();
            } else {
                compVal = Conversions.toRepr(outputComponent, context);
            }
            output.append(compVal);
        }
        return output.toString();
    }
}
