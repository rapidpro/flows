package io.rapidpro.excellent.parser;

import io.rapidpro.excellent.*;
import io.rapidpro.excellent.functions.Functions;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor for our expression trees
 */
public class ExcellentVisitorImpl extends ExcellentBaseVisitor<Object> {

    private EvaluationContext m_evalContext;

    public ExcellentVisitorImpl(EvaluationContext context) {
        this.m_evalContext = context;
    }

    @Override
    public Object visitFunctionCall(ExcellentParser.FunctionCallContext ctx) {
        String funcName = ctx.NAME().getText();
        List<Object> parameters;
        if (ctx.parameters() != null) {
            parameters = (List<Object>) visit(ctx.parameters());
        } else {
            parameters = Collections.emptyList();
        }
        return EvaluationUtils.invokeFunction(Functions.class, funcName, parameters);
    }

    @Override
    public Object visitFunctionParameters(ExcellentParser.FunctionParametersContext ctx) {
        return ctx.expression().stream().map(this::visit).collect(Collectors.toList());
    }

    @Override
    public Object visitNegation(ExcellentParser.NegationContext ctx) {
        return Conversions.toDecimal(visit(ctx.expression())).negate();
    }

    @Override
    public Object visitExponentExpression(ExcellentParser.ExponentExpressionContext ctx) {
        BigDecimal exp1 = Conversions.toDecimal(visit(ctx.expression(0)));
        BigDecimal exp2 = Conversions.toDecimal(visit(ctx.expression(1)));
        return EvaluationUtils.pow(exp1, exp2);
    }

    @Override
    public Object visitMultiplicationOrDivisionExpression(ExcellentParser.MultiplicationOrDivisionExpressionContext ctx) {
        boolean multiplication = ctx.TIMES() != null;

        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)));
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)));

        return multiplication ? arg1.multiply(arg2) : arg1.divide(arg2);
    }

    @Override
    public Object visitAdditionOrSubtractionExpression(ExcellentParser.AdditionOrSubtractionExpressionContext ctx) {
        boolean addition = ctx.PLUS() != null;

        BigDecimal arg1 = Conversions.toDecimal(visit(ctx.expression(0)));
        BigDecimal arg2 = Conversions.toDecimal(visit(ctx.expression(1)));

        return addition ? arg1.add(arg2) : arg1.subtract(arg2);
    }

    @Override
    public Object visitDecimalLiteral(ExcellentParser.DecimalLiteralContext ctx) {
        return new BigDecimal(ctx.DECIMAL().getText());
    }

    @Override
    public Object visitStringLiteral(ExcellentParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        String val = raw.substring(1, raw.length() - 1);  // remove enclosing quotes
        return val.replaceAll("\"\"", "\"");  // un-escape double quotes
    }

    @Override
    public Object visitTrue(ExcellentParser.TrueContext ctx) {
        return Boolean.TRUE;
    }

    @Override
    public Object visitFalse(ExcellentParser.FalseContext ctx) {
        return Boolean.FALSE;
    }

    @Override
    public Object visitContextReference(ExcellentParser.ContextReferenceContext ctx) {
        String identifier = ctx.NAME().getText();
        return m_evalContext.read(identifier);

    }

    @Override
    public Object visitParentheses(ExcellentParser.ParenthesesContext ctx) {
        return visit(ctx.expression());
    }
}
