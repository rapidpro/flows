package io.rapidpro.excellent.evaluator;

import io.rapidpro.excellent.*;
import io.rapidpro.excellent.functions.FunctionManager;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor for our expression trees
 */
public class ExpressionVisitorImpl extends ExcellentBaseVisitor<Object> {

    private FunctionManager m_functions;
    private EvaluationContext m_evalContext;

    public ExpressionVisitorImpl(FunctionManager functions, EvaluationContext context) {
        this.m_functions = functions;
        this.m_evalContext = context;
    }

    @Override
    public Object visitInput(ExcellentParser.InputContext ctx) {
        return visit(ctx.expression());
    }

    /**
     * expression : NAME LPAREN parameters? RPAREN
     */
    @Override
    public Object visitFunctionCall(ExcellentParser.FunctionCallContext ctx) {
        String funcName = ctx.NAME().getText();
        List<Object> parameters;
        if (ctx.parameters() != null) {
            parameters = (List<Object>) visit(ctx.parameters());
        } else {
            parameters = Collections.emptyList();
        }
        return m_functions.invokeFunction(m_evalContext, funcName, parameters);
    }

    /**
     * parameters : expression (COMMA expression)*
     */
    @Override
    public Object visitFunctionParameters(ExcellentParser.FunctionParametersContext ctx) {
        return ctx.expression().stream().map(this::visit).collect(Collectors.toList());
    }

    /**
     * expression: MINUS expression
     */
    @Override
    public Object visitNegation(ExcellentParser.NegationContext ctx) {
        return Conversions.toDecimal(visit(ctx.expression()), m_evalContext).negate();
    }

    /**
     * expression: expression EXPONENT expression
     */
    @Override
    public Object visitExponentExpression(ExcellentParser.ExponentExpressionContext ctx) {
        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)), m_evalContext);
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)), m_evalContext);
        return EvaluatorUtils.pow(arg1, arg2);
    }

    /**
     * expression: expression (TIMES | DIVIDE) expression
     */
    @Override
    public Object visitMultiplicationOrDivisionExpression(ExcellentParser.MultiplicationOrDivisionExpressionContext ctx) {
        boolean multiplication = ctx.TIMES() != null;

        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)), m_evalContext);
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)), m_evalContext);

        if (!multiplication && arg2.equals(BigDecimal.ZERO)) {
            throw new EvaluationError("Division by zero");
        }

        return multiplication ? arg1.multiply(arg2) : arg1.divide(arg2, 10, RoundingMode.HALF_UP);
    }

    /**
     * expression: expression (PLUS | MINUS) expression
     */
    @Override
    public Object visitAdditionOrSubtractionExpression(ExcellentParser.AdditionOrSubtractionExpressionContext ctx) {
        boolean add = ctx.PLUS() != null;
        Object arg1 = visit(ctx.expression(0));
        Object arg2 = visit(ctx.expression(1));

        // first try as decimals
        try {
            BigDecimal _arg1 = Conversions.toDecimal(arg1, m_evalContext);
            BigDecimal _arg2 = Conversions.toDecimal(arg2, m_evalContext);
            return add ? _arg1.add(_arg2) : _arg1.subtract(_arg2);
        } catch (EvaluationError ignored) {}

        // then as date + something
        try {
            Temporal _arg1 = Conversions.toDateOrDateTime(arg1, m_evalContext);
            TemporalAmount _arg2;

            if (arg2 instanceof OffsetTime) {
                // upgrade our date to datetime
                _arg1 = Conversions.toDateTime(_arg1, m_evalContext);

                // convert time value to a duration
                _arg2 = Duration.between(LocalTime.of(0, 0), ((OffsetTime) arg2).toLocalTime());
            }
            else {
                _arg2 = Period.ofDays(Conversions.toInteger(arg2, m_evalContext));
            }

            return add ? _arg1.plus(_arg2) : _arg1.minus(_arg2);

        } catch (EvaluationError ex) {
            throw new EvaluationError("Expression could not be evaluated as decimal or date arithmetic", ex);
        }
    }

    /**
     * expression: expression (LTE | LT | GTE | GT) expression
     */
    @Override
    public Object visitComparisonExpression(ExcellentParser.ComparisonExpressionContext ctx) {
        Pair<Object, Object> args = Conversions.toSame(visit(ctx.expression(0)), visit(ctx.expression(1)), m_evalContext);
        int compared;

        if (args.getLeft() instanceof String) {
            // string comparison is case-insensitive
            compared = ((String) args.getLeft()).compareToIgnoreCase((String) args.getRight());
        }
        else {
            compared = ((Comparable) args.getLeft()).compareTo(args.getRight());
        }

        if (ctx.LTE() != null) {
            return compared <= 0;
        } else if (ctx.LT() != null) {
            return compared < 0;
        } else if (ctx.GTE() != null) {
            return compared >= 0;
        } else { // GT
            return compared > 0;
        }
    }

    /**
     * expression: expression (EQ | NEQ) expression
     */
    @Override
    public Object visitEqualityExpression(ExcellentParser.EqualityExpressionContext ctx) {
        Pair<Object, Object> args = Conversions.toSame(visit(ctx.expression(0)), visit(ctx.expression(1)), m_evalContext);
        boolean equal;

        if (args.getLeft() instanceof String) {
            // string equality is case-insensitive
            equal = ((String) args.getLeft()).equalsIgnoreCase((String) args.getRight());
        }
        else if (args.getLeft() instanceof BigDecimal) {
            // compareTo doesn't take scale into account
            equal = ((BigDecimal) args.getLeft()).compareTo((BigDecimal) args.getRight()) == 0;
        }
        else {
            equal = args.getLeft().equals(args.getRight());
        }

        return ctx.EQ() != null ? equal : !equal;
    }

    /**
     * expression: expression AMPERSAND expression
     */
    @Override
    public Object visitConcatenation(ExcellentParser.ConcatenationContext ctx) {
        return Conversions.toString(visit(ctx.expression(0)), m_evalContext) + Conversions.toString(visit(ctx.expression(1)), m_evalContext);
    }

    /**
     * expression: STRING
     */
    @Override
    public Object visitStringLiteral(ExcellentParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        String val = raw.substring(1, raw.length() - 1);  // remove enclosing quotes
        return val.replaceAll("\"\"", "\"");  // un-escape double quotes
    }

    /**
     * expression: DECIMAL
     */
    @Override
    public Object visitDecimalLiteral(ExcellentParser.DecimalLiteralContext ctx) {
        return new BigDecimal(ctx.DECIMAL().getText());
    }

    /**
     * expression: TRUE
     */
    @Override
    public Object visitTrue(ExcellentParser.TrueContext ctx) {
        return Boolean.TRUE;
    }

    /**
     * expression: FALSE
     */
    @Override
    public Object visitFalse(ExcellentParser.FalseContext ctx) {
        return Boolean.FALSE;
    }

    /**
     * expression: NAME
     */
    @Override
    public Object visitContextReference(ExcellentParser.ContextReferenceContext ctx) {
        String identifier = ctx.NAME().getText();
        return m_evalContext.resolveVariable(identifier);
    }

    /**
     * expression: LPAREN expression RPAREN
     */
    @Override
    public Object visitParentheses(ExcellentParser.ParenthesesContext ctx) {
        return visit(ctx.expression());
    }
}
